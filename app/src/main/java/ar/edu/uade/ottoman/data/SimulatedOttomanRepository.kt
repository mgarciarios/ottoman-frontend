package ar.edu.uade.ottoman.data

import ar.edu.uade.ottoman.domain.AlertSeverity
import ar.edu.uade.ottoman.domain.CommandStatus
import ar.edu.uade.ottoman.domain.CommandGuard
import ar.edu.uade.ottoman.domain.CommandType
import ar.edu.uade.ottoman.domain.HistoryItem
import ar.edu.uade.ottoman.domain.MissionStatus
import ar.edu.uade.ottoman.domain.OttomanRepository
import ar.edu.uade.ottoman.domain.OttomanUiState
import ar.edu.uade.ottoman.domain.RobotAlert
import ar.edu.uade.ottoman.domain.RobotCommand
import ar.edu.uade.ottoman.domain.RobotTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.math.max

/** Simulación local: no abre sockets ni requiere hardware, servidor o conexión a Internet. */
class SimulatedOttomanRepository(
    private val historyStore: HistoryStore,
    private val commandResponseDelayMs: Long = 700L,
    private val noResponseTimeoutMs: Long = 4_000L,
    private val clock: () -> Long = System::currentTimeMillis,
) : OttomanRepository {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(OttomanUiState(history = historyStore.read()))
    override val state: StateFlow<OttomanUiState> = mutableState.asStateFlow()
    private var rejectNext = false
    private var runTicks = 0

    override suspend fun tick() = mutex.withLock {
        val current = mutableState.value
        val now = clock()
        val telemetry = current.telemetry
        if (!telemetry.connected) {
            val age = now - telemetry.lastUpdateAt
            mutableState.value = current.copy(telemetry = telemetry.copy(stale = age > STALE_AFTER_MS))
            return@withLock
        }
        var next = telemetry.copy(lastUpdateAt = now, stale = false)
        if (telemetry.missionStatus == MissionStatus.PREPARING) {
            next = next.copy(missionStatus = MissionStatus.RUNNING)
        } else if (telemetry.missionStatus == MissionStatus.RUNNING || telemetry.missionStatus == MissionStatus.RETURNING) {
            runTicks += 1
            next = next.copy(
                batteryPercent = max(0, telemetry.batteryPercent - if (runTicks % 4 == 0) 1 else 0),
                progressPercent = if (telemetry.missionStatus == MissionStatus.RETURNING) max(0, telemetry.progressPercent - 10) else (telemetry.progressPercent + 5).coerceAtMost(100),
                speedMetersPerSecond = 0.4,
            )
            next = next.copy(waypointIndex = (next.progressPercent / 25).coerceAtMost(3), waypoint = routePoint(next.waypointIndex, next.destination))
            if (telemetry.missionStatus == MissionStatus.RUNNING && next.progressPercent >= 100) {
                next = next.copy(missionStatus = MissionStatus.COMPLETED, speedMetersPerSecond = 0.0, waypoint = next.destination, waypointIndex = 4)
                addHistoryLocked("Misión completada", "Ottoman llegó a ${next.destination}.")
                addAlertLocked(AlertSeverity.INFO, "MISSION_COMPLETED", "Llegamos a ${next.destination}.", causeActive = false)
            } else if (telemetry.missionStatus == MissionStatus.RETURNING && next.progressPercent <= 0) {
                next = next.copy(missionStatus = MissionStatus.IDLE, destination = "Sin destino", speedMetersPerSecond = 0.0, waypoint = "Recepción", waypointIndex = 0, visitorPresent = false)
                addHistoryLocked("Regreso completado", "Ottoman volvió al punto seguro: Recepción.")
            }
        }
        mutableState.value = mutableState.value.copy(telemetry = next)
    }

    override suspend fun selectDestination(destination: String) = mutex.withLock {
        if (destination in DESTINATIONS) mutableState.value = mutableState.value.copy(selectedDestination = destination)
    }

    override suspend fun send(type: CommandType) {
        val command = mutex.withLock {
            val current = mutableState.value
            if (current.commands.any { it.status == CommandStatus.PENDING }) {
                val busy = RobotCommand(UUID.randomUUID().toString(), type, CommandStatus.REJECTED, clock(), "Hay otra orden pendiente.")
                mutableState.value = current.copy(commands = (listOf(busy) + current.commands).take(30))
                addHistoryLocked("Orden rechazada", "${label(type)}: ya existe una orden pendiente.")
                return
            }
            val reason = CommandGuard.rejectionReason(type, current)
            val status = if (!current.telemetry.connected && type != CommandType.ACK_ALERT) CommandStatus.REJECTED else CommandStatus.PENDING
            val cmd = RobotCommand(UUID.randomUUID().toString(), type, status, clock(), reason)
            mutableState.value = current.copy(commands = (listOf(cmd) + current.commands).take(30))
            addHistoryLocked(if (status == CommandStatus.PENDING) "Orden enviada" else "Orden rechazada", "${label(type)}${reason?.let { ": $it" } ?: ""}.")
            if (status != CommandStatus.PENDING) return
            cmd
        }

        val rejectThis = mutex.withLock { rejectNext.also { rejectNext = false; mutableState.value = mutableState.value.copy(rejectNextCommand = false) } }
        if (rejectThis) {
            delay(commandResponseDelayMs)
            finishCommand(command, CommandStatus.REJECTED, "Rechazada por el simulador (escenario de prueba).", applyAction = false)
            return
        }
        delay(commandResponseDelayMs)
        mutex.withLock {
            val current = mutableState.value
            val newReason = CommandGuard.rejectionReason(type, current)
            if (!current.telemetry.connected && type != CommandType.ACK_ALERT) {
                finishCommandLocked(command, CommandStatus.NO_RESPONSE, "Sin respuesta: se perdió la conexión.", applyAction = false)
            } else if (newReason != null) {
                finishCommandLocked(command, CommandStatus.REJECTED, newReason, applyAction = false)
            } else {
                finishCommandLocked(command, CommandStatus.CONFIRMED, null, applyAction = true)
            }
        }
    }

    private suspend fun finishCommand(command: RobotCommand, status: CommandStatus, reason: String?, applyAction: Boolean) = mutex.withLock {
        finishCommandLocked(command, status, reason, applyAction)
    }

    private fun finishCommandLocked(command: RobotCommand, status: CommandStatus, reason: String?, applyAction: Boolean) {
        val state = mutableState.value
        val updated = state.commands.map { if (it.id == command.id) it.copy(status = status, reason = reason) else it }
        var telemetry = state.telemetry
        if (applyAction) {
            telemetry = when (command.type) {
                CommandType.START -> {
                    runTicks = 0
                    telemetry.copy(missionStatus = MissionStatus.PREPARING, destination = state.selectedDestination, progressPercent = 0, waypoint = "Recepción", waypointIndex = 0, visitorPresent = true, missionId = UUID.randomUUID().toString(), speedMetersPerSecond = 0.0)
                }
                CommandType.PAUSE -> telemetry.copy(missionStatus = MissionStatus.PAUSED, speedMetersPerSecond = 0.0)
                CommandType.RESUME -> telemetry.copy(missionStatus = MissionStatus.RUNNING, speedMetersPerSecond = 0.4)
                CommandType.CANCEL -> telemetry.copy(missionStatus = MissionStatus.CANCELLED, speedMetersPerSecond = 0.0, visitorPresent = false)
                CommandType.RETURN_SAFE -> telemetry.copy(missionStatus = MissionStatus.RETURNING, speedMetersPerSecond = 0.4)
                CommandType.OPERATIONAL_STOP -> telemetry.copy(missionStatus = MissionStatus.STOPPED, speedMetersPerSecond = 0.0)
                CommandType.ACK_ALERT -> telemetry
            }
            if (command.type == CommandType.START) {
                addHistoryLocked("Misión iniciada", "Recepción → ${state.selectedDestination}. Acompañamiento simulado.")
                delaylessAddAlertLocked(AlertSeverity.INFO, "VISITOR_ACCOMPANIED", "Visitante acompañado (simulado).", causeActive = false)
            } else addHistoryLocked("Orden confirmada", "${label(command.type)}: aceptada por el simulador. La ejecución se verifica con telemetría.")
            if (command.type == CommandType.CANCEL || command.type == CommandType.OPERATIONAL_STOP) {
                addHistoryLocked(if (command.type == CommandType.CANCEL) "Misión cancelada" else "Detención operativa simulada", "El robot simulado no equivale a una parada física de emergencia.")
            }
            if (command.type == CommandType.RETURN_SAFE) addHistoryLocked("Regreso solicitado", "Destino seguro: Recepción.")
        } else {
            addHistoryLocked(if (status == CommandStatus.NO_RESPONSE) "Orden sin respuesta" else "Orden rechazada", "${label(command.type)}${reason?.let { ": $it" } ?: ""}.")
        }
        mutableState.value = state.copy(telemetry = telemetry, alerts = mutableState.value.alerts, commands = updated, history = mutableState.value.history)
        persistHistoryLocked()
    }

    override suspend fun acknowledge(alertId: String) = mutex.withLock {
        val alerts = mutableState.value.alerts.map { if (it.id == alertId) it.copy(acknowledged = true) else it }
        mutableState.value = mutableState.value.copy(alerts = alerts)
        addHistoryLocked("Alerta reconocida", "${alerts.firstOrNull { it.id == alertId }?.message ?: "Alerta"} Reconocer no resuelve la causa.")
        val command = RobotCommand(UUID.randomUUID().toString(), CommandType.ACK_ALERT, CommandStatus.CONFIRMED, clock())
        mutableState.value = mutableState.value.copy(commands = (listOf(command) + mutableState.value.commands).take(30))
        persistHistoryLocked()
    }

    override suspend fun disconnect() = mutex.withLock {
        if (!mutableState.value.telemetry.connected) return@withLock
        mutableState.value = mutableState.value.copy(telemetry = mutableState.value.telemetry.copy(connected = false, stale = false))
        addHistoryLocked("Robot desconectado", "La última telemetría queda congelada hasta la reconexión.")
        persistHistoryLocked()
    }

    override suspend fun reconnect() = mutex.withLock {
        mutableState.value = mutableState.value.copy(telemetry = mutableState.value.telemetry.copy(connected = true, stale = false, lastUpdateAt = clock()))
        addHistoryLocked("Robot reconectado", "Se recibió una nueva telemetría del simulador.")
        persistHistoryLocked()
    }

    override suspend fun forceLowBattery() = mutex.withLock {
        mutableState.value = mutableState.value.copy(telemetry = mutableState.value.telemetry.copy(batteryPercent = 18, lastUpdateAt = clock(), stale = false))
        addAlertLocked(AlertSeverity.WARNING, "LOW_BATTERY", "Batería baja: 18%. No se permiten nuevas misiones.")
        addHistoryLocked("Escenario: batería baja", "Batería simulada fijada en 18%.")
        persistHistoryLocked()
    }

    override suspend fun forceObstacle() = mutex.withLock {
        var t = mutableState.value.telemetry
        if (t.missionStatus !in ACTIVE_MISSIONS) {
            t = t.copy(missionStatus = MissionStatus.RUNNING, destination = "Aula Magna", progressPercent = 25, waypoint = "Hall central", waypointIndex = 1, visitorPresent = true, missionId = UUID.randomUUID().toString(), speedMetersPerSecond = 0.4)
        }
        mutableState.value = mutableState.value.copy(telemetry = t.copy(missionStatus = MissionStatus.BLOCKED, speedMetersPerSecond = 0.0, lastUpdateAt = clock()))
        addAlertLocked(AlertSeverity.CRITICAL, "OBSTACLE", "Obstáculo en Pasillo A. Ottoman espera una decisión del operador.")
        addHistoryLocked("Escenario: obstáculo", "Movimiento detenido. Resolver la causa antes de reanudar.")
        persistHistoryLocked()
    }

    override suspend fun resolveObstacle() = mutex.withLock {
        mutableState.value = mutableState.value.copy(alerts = mutableState.value.alerts.map { if (it.type == "OBSTACLE" && it.causeActive) it.copy(causeActive = false) else it })
        addHistoryLocked("Obstáculo resuelto", "Resolución manual simulada por el operador.")
        persistHistoryLocked()
    }

    override suspend fun separateVisitor() = mutex.withLock {
        var t = mutableState.value.telemetry
        if (t.missionStatus !in ACTIVE_MISSIONS) t = t.copy(missionStatus = MissionStatus.RUNNING, destination = "Aula Magna", progressPercent = 30, waypoint = "Hall central", waypointIndex = 1, missionId = UUID.randomUUID().toString())
        mutableState.value = mutableState.value.copy(telemetry = t.copy(missionStatus = MissionStatus.PAUSED, visitorPresent = false, speedMetersPerSecond = 0.0, lastUpdateAt = clock()))
        addAlertLocked(AlertSeverity.CRITICAL, "VISITOR_SEPARATED", "Acompañamiento interrumpido. La separación es un evento de demo, no una detección real.")
        addHistoryLocked("Escenario propio: visitante separado", "Misión pausada; se requiere intervención del operador.")
        persistHistoryLocked()
    }

    override suspend fun reuniteVisitor() = mutex.withLock {
        mutableState.value = mutableState.value.copy(
            telemetry = mutableState.value.telemetry.copy(visitorPresent = true, lastUpdateAt = clock()),
            alerts = mutableState.value.alerts.map { if (it.type == "VISITOR_SEPARATED" && it.causeActive) it.copy(causeActive = false) else it },
        )
        addHistoryLocked("Visitante reunido", "Restablecimiento manual de la demo; la misión sigue pausada hasta reanudarla.")
        persistHistoryLocked()
    }

    override suspend fun rejectNextCommand() = mutex.withLock {
        rejectNext = true
        mutableState.value = mutableState.value.copy(rejectNextCommand = true)
        addHistoryLocked("Demo preparada", "La próxima orden será rechazada por el simulador.")
        persistHistoryLocked()
    }

    override suspend fun simulateNoResponse() {
        val command = mutex.withLock {
            if (!mutableState.value.telemetry.connected) return
            val pending = RobotCommand(UUID.randomUUID().toString(), CommandType.PAUSE, CommandStatus.PENDING, clock())
            mutableState.value = mutableState.value.copy(
                commands = (listOf(pending) + mutableState.value.commands).take(30),
                telemetry = mutableState.value.telemetry.copy(connected = false),
            )
            addHistoryLocked("Orden enviada", "PAUSAR enviada antes de la desconexión; se espera respuesta.")
            addHistoryLocked("Robot desconectado", "Se congeló la telemetría mientras la orden está pendiente.")
            persistHistoryLocked()
            pending
        }
        delay(noResponseTimeoutMs)
        mutex.withLock { finishCommandLocked(command, CommandStatus.NO_RESPONSE, "Timeout: el simulador no recibió confirmación.", applyAction = false) }
    }

    override suspend fun resetRobot() = mutex.withLock {
        runTicks = 0
        mutableState.value = mutableState.value.copy(
            telemetry = RobotTelemetry(lastUpdateAt = clock()),
            alerts = emptyList(),
            commands = emptyList(),
            selectedDestination = "Aula Magna",
            rejectNextCommand = false,
        )
        rejectNext = false
        addHistoryLocked("Robot restablecido", "Estado de demo reiniciado; el historial local se conserva.")
        persistHistoryLocked()
    }

    private fun routePoint(index: Int, destination: String): String = when (index) {
        0 -> "Recepción"
        1 -> "Hall central"
        2 -> "Pasillo A"
        else -> destination
    }

    private fun addAlertLocked(severity: AlertSeverity, type: String, message: String, causeActive: Boolean = true) {
        delaylessAddAlertLocked(severity, type, message, causeActive)
    }

    private fun delaylessAddAlertLocked(severity: AlertSeverity, type: String, message: String, causeActive: Boolean) {
        val alert = RobotAlert(UUID.randomUUID().toString(), severity, type, message, clock(), causeActive = causeActive)
        mutableState.value = mutableState.value.copy(alerts = listOf(alert) + mutableState.value.alerts)
    }

    private fun addHistoryLocked(title: String, detail: String) {
        val item = HistoryItem(UUID.randomUUID().toString(), title, detail, clock())
        val history = (listOf(item) + mutableState.value.history).take(200)
        mutableState.value = mutableState.value.copy(history = history)
        persistHistoryLocked()
    }

    private fun persistHistoryLocked() {
        historyStore.write(mutableState.value.history)
    }

    private fun label(type: CommandType): String = when (type) {
        CommandType.START -> "INICIAR MISIÓN"
        CommandType.PAUSE -> "PAUSAR MISIÓN"
        CommandType.RESUME -> "REANUDAR MISIÓN"
        CommandType.CANCEL -> "CANCELAR MISIÓN"
        CommandType.RETURN_SAFE -> "REGRESAR A RECEPCIÓN"
        CommandType.OPERATIONAL_STOP -> "DETENCIÓN OPERATIVA (SIMULADA)"
        CommandType.ACK_ALERT -> "RECONOCER ALERTA"
    }

    companion object {
        const val MIN_START_BATTERY = CommandGuard.MIN_START_BATTERY
        private const val STALE_AFTER_MS = 5_000L
        val DESTINATIONS = listOf("Aula Magna", "Aula 204", "Laboratorio de Tecnología")
        private val ACTIVE_MISSIONS = setOf(MissionStatus.PREPARING, MissionStatus.RUNNING, MissionStatus.PAUSED, MissionStatus.BLOCKED, MissionStatus.RETURNING)
    }
}
