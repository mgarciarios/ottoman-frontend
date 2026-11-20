package ar.edu.uade.ottoman.domain

/** Reglas de habilitación compartidas por el simulador y las pruebas unitarias. */
object CommandGuard {
    const val MIN_START_BATTERY = 25
    private const val CRITICAL_BATTERY = 10
    private val activeMissionStatuses = setOf(MissionStatus.PREPARING, MissionStatus.RUNNING, MissionStatus.PAUSED, MissionStatus.BLOCKED, MissionStatus.RETURNING)

    fun rejectionReason(type: CommandType, state: OttomanUiState): String? {
        val t = state.telemetry
        if (!t.connected && type != CommandType.ACK_ALERT) return "Robot desconectado; la orden no se envió."
        return when (type) {
            CommandType.START -> when {
                t.missionStatus !in setOf(MissionStatus.IDLE, MissionStatus.COMPLETED, MissionStatus.CANCELLED, MissionStatus.FAILED) -> "Ya hay una misión activa."
                t.batteryPercent < MIN_START_BATTERY -> "Batería insuficiente para iniciar (mínimo $MIN_START_BATTERY%)."
                else -> null
            }
            CommandType.PAUSE -> if (t.missionStatus in setOf(MissionStatus.RUNNING, MissionStatus.RETURNING)) null else "No hay misión en movimiento para pausar."
            CommandType.RESUME -> when {
                t.missionStatus != MissionStatus.PAUSED && t.missionStatus != MissionStatus.BLOCKED -> "La misión no está pausada."
                t.batteryPercent < MIN_START_BATTERY -> "Batería insuficiente para reanudar."
                state.alerts.any { it.causeActive && it.type == "OBSTACLE" } -> "Primero hay que resolver el obstáculo."
                state.alerts.any { it.causeActive && it.type == "VISITOR_SEPARATED" } -> "Esperá a que el visitante vuelva al acompañamiento."
                else -> null
            }
            CommandType.CANCEL -> if (t.missionStatus in activeMissionStatuses) null else "No hay misión activa para cancelar."
            CommandType.RETURN_SAFE -> when {
                t.missionStatus !in activeMissionStatuses && t.missionStatus != MissionStatus.COMPLETED -> "No hay un recorrido para regresar."
                t.batteryPercent < CRITICAL_BATTERY -> "Batería crítica; requiere intervención del operador."
                else -> null
            }
            CommandType.OPERATIONAL_STOP -> if (t.missionStatus == MissionStatus.STOPPED) "El robot ya está detenido en la simulación." else null
            CommandType.ACK_ALERT -> null
        }
    }
}
