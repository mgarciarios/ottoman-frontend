package ar.edu.uade.ottoman.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.uade.ottoman.domain.AlertSeverity
import ar.edu.uade.ottoman.domain.CommandGuard
import ar.edu.uade.ottoman.domain.CommandStatus
import ar.edu.uade.ottoman.domain.CommandType
import ar.edu.uade.ottoman.domain.HistoryItem
import ar.edu.uade.ottoman.domain.MissionStatus
import ar.edu.uade.ottoman.domain.OttomanUiState
import ar.edu.uade.ottoman.domain.RobotAlert
import ar.edu.uade.ottoman.domain.RobotCommand
import ar.edu.uade.ottoman.domain.RobotTelemetry
import ar.edu.uade.ottoman.presentation.OttomanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Navy = Color(0xFF174A67)
private val Ink = Color(0xFF172430)
private val Muted = Color(0xFF647582)
private val CanvasBg = Color(0xFFF3F7F9)
private val Green = Color(0xFF167A61)
private val Amber = Color(0xFFB16A00)
private val Red = Color(0xFFB3261E)
private val Line = Color(0xFFE2E9ED)
private val destinations = listOf("Aula Magna", "Aula 204", "Laboratorio de Tecnología")
private val navItems = listOf("Inicio", "Misiones", "Alertas", "Historial", "Demo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OttomanApp(viewModel: OttomanViewModel) {
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf("Inicio") }
    var confirm by remember { mutableStateOf<CommandType?>(null) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, secondary = Green, background = CanvasBg, surface = Color.White, error = Red)) {
        Scaffold(
            containerColor = CanvasBg,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(when (selected) { "Telemetría" -> "Telemetría"; "Robot" -> "Seleccionar robot"; else -> "Ottoman Control" }, fontWeight = FontWeight.Bold, color = Ink)
                            Text(if (selected == "Telemetría" || selected == "Robot") "Unitree G1 · datos de simulación" else "SUPERVISIÓN · OTTOMAN-01", fontSize = 10.sp, letterSpacing = 1.1.sp, color = Muted)
                        }
                    },
                    navigationIcon = {
                        if (selected == "Telemetría" || selected == "Robot") TextButton(onClick = { selected = "Inicio" }) { Text("‹  Inicio", color = Navy) }
                    },
                    actions = {
                        if (selected != "Robot") TextButton(onClick = { selected = "Robot" }) { Text("Ottoman-01", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        Text("SIMULADO", color = Navy, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 10.dp))
                    },
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                    navItems.forEachIndexed { index, name ->
                        NavigationBarItem(
                            selected = selected == name,
                            onClick = { selected = name },
                            icon = { Text(listOf("⌂", "◎", "!", "≡", "⚙")[index], fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                            label = { Text(name, fontSize = 10.sp, maxLines = 1) },
                        )
                    }
                }
            },
        ) { padding ->
            val modifier = Modifier.fillMaxSize().padding(padding)
            when (selected) {
                "Inicio" -> Dashboard(state, viewModel, modifier, onOpenTelemetry = { selected = "Telemetría" }, onConfirm = { confirm = it }, onOpenMission = { selected = "Misiones" })
                "Misiones" -> Missions(state, viewModel, modifier, onConfirm = { confirm = it })
                "Alertas" -> Alerts(state, viewModel, modifier)
                "Historial" -> History(state.history, modifier)
                "Demo" -> DemoLab(state, viewModel, modifier)
                "Robot" -> RobotSelection(state.telemetry, modifier) { selected = "Inicio" }
                else -> Telemetry(state.telemetry, modifier)
            }
        }
        confirm?.let { command ->
            val cancel = command == CommandType.CANCEL
            AlertDialog(
                onDismissRequest = { confirm = null },
                title = { Text(if (cancel) "¿Cancelar la misión?" else "¿Solicitar detención operativa?") },
                text = {
                    Text(if (cancel) "Se enviará la solicitud al simulador. La misión solo cambia al confirmarse." else "Esta acción es simulada y no reemplaza una parada de emergencia física.")
                },
                confirmButton = { TextButton(onClick = { viewModel.send(command); confirm = null }) { Text(if (cancel) "Confirmar cancelación" else "Confirmar detención") } },
                dismissButton = { TextButton(onClick = { confirm = null }) { Text("Volver") } },
            )
        }
    }
}

@Composable
private fun Dashboard(state: OttomanUiState, vm: OttomanViewModel, modifier: Modifier, onOpenTelemetry: () -> Unit, onConfirm: (CommandType) -> Unit, onOpenMission: () -> Unit) {
    val t = state.telemetry
    ScreenColumn(modifier) {
        if (!t.connected) OfflineBanner(t)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("BATERÍA", "${t.batteryPercent}%", if (t.batteryPercent <= 20) Amber else Green, Modifier.weight(1f), "▰")
            MetricCard("CONEXIÓN", if (t.connected) "En línea" else "Sin conexión", if (t.connected) Green else Red, Modifier.weight(1f), "◉")
        }
        CardBlock {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("ESTADO OPERATIVO")
                    Text(missionLabel(t.missionStatus), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(if (t.missionStatus == MissionStatus.RUNNING) "Ottoman acompaña una visita" else stateDescription(t.missionStatus), color = Muted, fontSize = 13.sp)
                }
                StatusPill(if (t.missionStatus == MissionStatus.RUNNING) "EN MISIÓN" else missionLabel(t.missionStatus).uppercase(), missionColor(t.missionStatus))
            }
            Spacer(Modifier.height(16.dp))
            Hairline()
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("MISIÓN ACTUAL")
                    Text(if (t.destination == "Sin destino") "Sin misión activa" else "Recepción → ${t.destination}", fontWeight = FontWeight.SemiBold, color = Ink)
                }
                Text("${t.progressPercent}%", color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { t.progressPercent / 100f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = Navy, trackColor = Line)
            Spacer(Modifier.height(8.dp))
            Text("${t.waypoint} · ${t.waypointIndex.coerceAtMost(4)} de ${t.waypointCount} tramos", color = Muted, fontSize = 12.sp)
        }
        CardBlock {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("RECORRIDO ESQUEMÁTICO")
                    Text("Campus demostrativo · planta baja", fontWeight = FontWeight.SemiBold, color = Ink)
                }
                TextButton(onClick = onOpenTelemetry) { Text("Telemetría →", color = Navy) }
            }
            Spacer(Modifier.height(8.dp))
            RouteMap(t)
            Text("Mapa ilustrativo, no representa un plano verificado de UADE.", color = Muted, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("DESTINO", if (t.destination == "Sin destino") state.selectedDestination else t.destination, Navy, Modifier.weight(1f), "↗")
            MetricCard("VISITANTE", if (t.visitorPresent) "Acompañado" else "No asignado", if (t.visitorPresent) Green else Muted, Modifier.weight(1f), "●")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("RECORRIDO", "${t.waypointIndex}/${t.waypointCount} tramos", Navy, Modifier.weight(1f), "⌁")
            MetricCard("VELOCIDAD", "%.1f m/s".format(Locale.US, t.speedMetersPerSecond), Navy, Modifier.weight(1f), "↗")
        }
        VisitorMessageCard(t)
        CardBlock {
            SectionTitle("Controles rápidos", "Las órdenes requieren confirmación del simulador")
            Spacer(Modifier.height(10.dp))
            CommandControls(state, vm, onConfirm, compact = true)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenMission, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("Ver misión y controles") }
        }
        RecentCommands(state.commands)
        Text("Última telemetría: ${clock(t.lastUpdateAt)} · ${if (t.stale) "ANTIGUA" else "actualizada"}", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
    }
}

@Composable
private fun Missions(state: OttomanUiState, vm: OttomanViewModel, modifier: Modifier, onConfirm: (CommandType) -> Unit) {
    val t = state.telemetry
    ScreenColumn(modifier) {
        SectionTitle("Misiones", "Elegí el destino y supervisá las órdenes")
        if (t.missionStatus in activeMissionStatuses) {
            CardBlock {
                Eyebrow("MISIÓN EN CURSO")
                Text("Recepción → ${t.destination}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { t.progressPercent / 100f }, modifier = Modifier.fillMaxWidth(), color = Navy)
                Text("${t.progressPercent}% · ${t.waypoint}", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            Text("Punto de partida · Recepción", color = Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            destinations.forEach { destination ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { vm.selectDestination(destination) },
                    colors = CardDefaults.cardColors(containerColor = if (state.selectedDestination == destination) Color(0xFFE8F1F6) else Color.White),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(if (state.selectedDestination == destination) "●" else "○", color = Navy, fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(destination, color = Ink, fontWeight = FontWeight.SemiBold)
                            Text("Ruta simulada desde Recepción", color = Muted, fontSize = 12.sp)
                        }
                        Text("→", color = Navy, fontSize = 20.sp)
                    }
                }
            }
            Text("No se usa un mapa real ni se capturan datos del visitante.", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { vm.send(CommandType.START) },
                enabled = t.connected && t.batteryPercent >= CommandGuard.MIN_START_BATTERY && t.missionStatus in setOf(MissionStatus.IDLE, MissionStatus.COMPLETED, MissionStatus.CANCELLED, MissionStatus.FAILED),
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Navy),
            ) { Text("Iniciar acompañamiento") }
            if (t.batteryPercent < CommandGuard.MIN_START_BATTERY) Text("Inicio bloqueado: se requieren al menos ${CommandGuard.MIN_START_BATTERY}% de batería.", color = Amber, fontSize = 12.sp)
        }
        CardBlock {
            SectionTitle("Control del robot", "Estado de cada orden visible debajo")
            Spacer(Modifier.height(10.dp))
            CommandControls(state, vm, onConfirm, compact = false)
        }
        RecentCommands(state.commands)
        VisitorMessageCard(t)
    }
}

@Composable
private fun RobotSelection(t: RobotTelemetry, modifier: Modifier, onSelect: () -> Unit) {
    ScreenColumn(modifier) {
        SectionTitle("Robot disponible", "Elegí el equipo que supervisará esta sesión")
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("UNITREE G1 · HUMANOIDE")
                        Text("Ottoman-01", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    StatusPill("SELECCIONADO", Green)
                }
                Hairline()
                DetailRow("Conectividad", if (t.connected) "En línea · simulada" else "Sin conexión")
                DetailRow("Batería", "${t.batteryPercent}% · dato ficticio")
                DetailRow("Estado", missionLabel(t.missionStatus))
            }
        }
        Text("Esta versión académica incluye un solo robot. La selección identifica el Ottoman G1 simulado y no establece conexión con hardware.", color = Muted, fontSize = 12.sp)
        Button(onClick = onSelect, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Navy)) {
            Text("Usar Ottoman-01")
        }
    }
}

@Composable
private fun VisitorMessageCard(t: RobotTelemetry) {
    CardBlock {
        Eyebrow("COMUNICACIÓN HRI · SIMULADA")
        Text(hriMessage(t), color = Ink, fontWeight = FontWeight.Medium)
        Text("Texto de demostración; no hay audio ni interacción física con personas.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun CommandControls(state: OttomanUiState, vm: OttomanViewModel, onConfirm: (CommandType) -> Unit, compact: Boolean) {
    val t = state.telemetry
    val pending = state.commands.any { it.status == CommandStatus.PENDING }
    val enabledConnected = t.connected && !pending
    val pauseEnabled = enabledConnected && t.missionStatus in setOf(MissionStatus.RUNNING, MissionStatus.RETURNING)
    val resumeEnabled = enabledConnected && t.batteryPercent >= CommandGuard.MIN_START_BATTERY && t.missionStatus in setOf(MissionStatus.PAUSED, MissionStatus.BLOCKED)
    val canCancel = enabledConnected && t.missionStatus in activeMissionStatuses
    val canReturn = enabledConnected && (t.missionStatus in activeMissionStatuses || t.missionStatus == MissionStatus.COMPLETED)
    val canStart = enabledConnected && t.batteryPercent >= 25 && t.missionStatus in setOf(MissionStatus.IDLE, MissionStatus.COMPLETED, MissionStatus.CANCELLED, MissionStatus.FAILED)
    val rows = if (compact) listOf(
        Triple("Pausar", CommandType.PAUSE, pauseEnabled), Triple("Reanudar", CommandType.RESUME, resumeEnabled),
        Triple("Volver a Recepción", CommandType.RETURN_SAFE, canReturn), Triple("Detención operativa", CommandType.OPERATIONAL_STOP, enabledConnected && t.missionStatus != MissionStatus.STOPPED),
    ) else listOf(
        Triple("Iniciar", CommandType.START, canStart), Triple("Pausar", CommandType.PAUSE, pauseEnabled),
        Triple("Reanudar", CommandType.RESUME, resumeEnabled), Triple("Cancelar misión", CommandType.CANCEL, canCancel),
        Triple("Volver a Recepción", CommandType.RETURN_SAFE, canReturn), Triple("Detención operativa · simulada", CommandType.OPERATIONAL_STOP, enabledConnected && t.missionStatus != MissionStatus.STOPPED),
    )
    rows.chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            row.forEach { (title, type, enabled) ->
                Button(
                    onClick = { if (type == CommandType.CANCEL || type == CommandType.OPERATIONAL_STOP) onConfirm(type) else vm.send(type) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f).height(if (compact) 54.dp else 58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (type == CommandType.CANCEL || type == CommandType.OPERATIONAL_STOP) Color(0xFFFFF0EE) else Navy, contentColor = if (type == CommandType.CANCEL || type == CommandType.OPERATIONAL_STOP) Red else Color.White, disabledContainerColor = Color(0xFFE8ECEF), disabledContentColor = Muted),
                ) { Text(title, fontSize = if (compact) 11.sp else 12.sp, lineHeight = 14.sp) }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
    if (!t.connected) Text("Órdenes bloqueadas hasta recuperar conexión.", color = Red, fontSize = 12.sp)
    if (pending) Text("Hay una orden pendiente de respuesta…", color = Amber, fontSize = 12.sp)
    if (t.batteryPercent < CommandGuard.MIN_START_BATTERY && t.missionStatus in setOf(MissionStatus.PAUSED, MissionStatus.BLOCKED)) {
        Text("Reanudación bloqueada por batería baja; solicitá regreso seguro o intervención.", color = Amber, fontSize = 12.sp)
    }
}

@Composable
private fun Telemetry(t: RobotTelemetry, modifier: Modifier) {
    ScreenColumn(modifier) {
        if (!t.connected) OfflineBanner(t)
        SectionTitle("Telemetría", "Último snapshot local · Ottoman-01")
        CardBlock {
            Eyebrow("ACTUALIZACIÓN")
            Text(if (t.stale) "Datos antiguos" else "${clock(t.lastUpdateAt)} · actualizados", color = if (t.stale) Red else Green, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Edad: ${age(t.lastUpdateAt)} · ${if (t.connected) "en línea" else "telemetría congelada"}", color = Muted, fontSize = 12.sp)
        }
        CardBlock {
            SectionTitle("Estado del robot", "Unitree G1 · simulación")
            DetailRow("Conectividad", if (t.connected) "Conectado" else "Desconectado")
            DetailRow("Batería", "${t.batteryPercent}%")
            DetailRow("Estado operativo", missionLabel(t.missionStatus))
            DetailRow("Misión / progreso", "${t.destination} · ${t.progressPercent}%")
            DetailRow("Ubicación", t.waypoint)
            DetailRow("Movimiento", if (t.speedMetersPerSecond > 0) "En movimiento" else "Detenido")
            DetailRow("Velocidad simulada", "%.1f m/s".format(Locale.US, t.speedMetersPerSecond))
        }
        CardBlock {
            SectionTitle("Variables del servicio", "Acompañamiento universitario")
            DetailRow("Destino elegido", t.destination)
            DetailRow("Visitante presente", if (t.visitorPresent) "Sí · simulado" else "No asignado / separado")
            DetailRow("Tramos completados", "${t.waypointIndex.coerceAtMost(t.waypointCount)} de ${t.waypointCount}")
        }
    }
}

@Composable
private fun Alerts(state: OttomanUiState, vm: OttomanViewModel, modifier: Modifier) {
    ScreenColumn(modifier) {
        SectionTitle("Alertas", "Reconocer no elimina ni resuelve la causa")
        if (state.alerts.isEmpty()) EmptyCard("Todo en orden", "No hay alertas en el simulador.")
        state.alerts.forEach { alert -> AlertCard(alert) { vm.acknowledge(alert.id) } }
        if (state.alerts.isNotEmpty()) Text("Las alertas y su reconocimiento quedan en el historial local.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun History(items: List<HistoryItem>, modifier: Modifier) {
    ScreenColumn(modifier) {
        SectionTitle("Historial", "Eventos locales · persistentes en este dispositivo")
        if (items.isEmpty()) EmptyCard("Todavía no hay eventos", "Iniciá una misión o probá un escenario desde Demo.")
        items.forEach { item -> HistoryCard(item) }
        Text("Historial guardado localmente con SharedPreferences. No se envían datos a un servidor.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
    }
}

@Composable
private fun DemoLab(state: OttomanUiState, vm: OttomanViewModel, modifier: Modifier) {
    ScreenColumn(modifier) {
        CardBlock {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow("MODO DE DEMOSTRACIÓN")
                    Text("Laboratorio de escenarios", fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Ink)
                }
                StatusPill("SIMULACIÓN", Navy)
            }
            Text("Todos los controles afectan solo al robot virtual. Cada acción registra un evento.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }
        Button(onClick = vm::normalDemo, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Navy)) { Text("▶  Ejecutar misión normal") }
        DemoAction("Batería baja · 18%", "Muestra aviso y bloquea nuevas misiones", Amber) { vm.lowBattery() }
        if (state.telemetry.connected) DemoAction("Desconectar robot", "Congela la última telemetría", Red) { vm.disconnect() }
        else DemoAction("Reconectar robot", "Recibe una nueva telemetría", Green) { vm.reconnect() }
        DemoAction("Forzar obstáculo", "Pausa el avance y solicita decisión", Red) { vm.obstacle() }
        DemoAction("Resolver obstáculo", "Despeja la causa; luego se puede reanudar", Green) { vm.resolveObstacle() }
        DemoAction("Visitante se separa", "Pausa; evento simulado, no detección real", Red) { vm.separateVisitor() }
        DemoAction("Visitante vuelve", "Restablece acompañamiento para reanudar", Green) { vm.reuniteVisitor() }
        DemoAction("Rechazar próxima orden", "La siguiente orden devuelve REJECTED", Amber) { vm.rejectNext() }
        if (state.telemetry.connected) DemoAction("Orden sin respuesta", "Envía PAUSE y corta conexión; timeout 4 s", Amber) { vm.noResponse() }
        CardBlock {
            Text("La detención operativa es una orden de software simulada; no equivale a un paro de emergencia físico.", color = Red, fontWeight = FontWeight.Medium, fontSize = 12.sp)
        }
        TextButton(onClick = vm::reset, modifier = Modifier.fillMaxWidth()) { Text("Restablecer robot (conserva historial)", color = Navy) }
    }
}

@Composable
private fun RecentCommands(commands: List<RobotCommand>) {
    CardBlock {
        SectionTitle("Órdenes recientes", "PENDING · CONFIRMED · REJECTED · NO_RESPONSE")
        if (commands.isEmpty()) Text("Aún no se enviaron órdenes.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
        commands.take(4).forEach { command ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(command.type.displayName(), color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(clock(command.createdAt), color = Muted, fontSize = 11.sp)
                    command.reason?.let { Text(it, color = Red, fontSize = 11.sp) }
                }
                StatusPill(command.status.displayName(), command.status.color())
            }
        }
    }
}

@Composable
private fun AlertCard(alert: RobotAlert, onAcknowledge: () -> Unit) {
    val color = alert.severity.color()
    CardBlock {
        Row(verticalAlignment = Alignment.Top) {
            Text(if (alert.severity == AlertSeverity.CRITICAL) "!" else "●", color = color, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.severity.displayName() + if (alert.causeActive) " · ACTIVA" else " · RESUELTA", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
                Text(alert.message, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                Text(clock(alert.createdAt), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            Text(if (alert.acknowledged) "Reconocida · la causa puede seguir activa" else "Pendiente de reconocimiento", color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            if (!alert.acknowledged) TextButton(onClick = onAcknowledge) { Text("Reconocer", color = Navy) }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItem) {
    CardBlock {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(Navy).padding(1.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = Ink, fontWeight = FontWeight.SemiBold)
                Text(item.detail, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                Text(clock(item.timestamp), color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun DemoAction(title: String, subtitle: String, tint: Color, action: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(onClick = action),
        shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = Muted, fontSize = 11.sp)
            }
            Text("›", color = Navy, fontSize = 22.sp)
        }
    }
}

@Composable
private fun RouteMap(t: RobotTelemetry) {
    val position = (t.progressPercent / 100f).coerceIn(0f, 1f)
    val nodes = listOf(0.08f, 0.36f, 0.66f, 0.92f)
    Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F8FA))) {
        Canvas(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 23.dp)) {
            val y = size.height * 0.55f
            val path = Path().apply {
                moveTo(size.width * 0.05f, y)
                cubicTo(size.width * 0.22f, y - 16f, size.width * 0.22f, y - 16f, size.width * 0.38f, y)
                cubicTo(size.width * 0.54f, y + 17f, size.width * 0.54f, y + 17f, size.width * 0.69f, y)
                lineTo(size.width * 0.95f, y)
            }
            drawPath(path, Color(0xFFD4E0E6), style = Stroke(width = 7f, cap = StrokeCap.Round))
            drawPath(path, Navy, style = Stroke(width = 3f, cap = StrokeCap.Round))
            nodes.forEachIndexed { index, x ->
                val done = index <= (t.progressPercent / 25f).toInt()
                drawCircle(if (done) Green else Color.White, radius = 7f, center = androidx.compose.ui.geometry.Offset(size.width * x, y))
                drawCircle(if (done) Green else Muted, radius = 7f, center = androidx.compose.ui.geometry.Offset(size.width * x, y), style = Stroke(width = 2f))
            }
            val px = size.width * (0.05f + position * 0.9f)
            drawCircle(Color.White, radius = 12f, center = androidx.compose.ui.geometry.Offset(px, y))
            drawCircle(Navy, radius = 9f, center = androidx.compose.ui.geometry.Offset(px, y))
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 11.dp, vertical = 6.dp)) {
            Text("Recepción", color = Muted, fontSize = 9.sp)
            Text("Hall central", color = Muted, fontSize = 9.sp)
            Text("Pasillo A", color = Muted, fontSize = 9.sp)
            Text(if (t.destination == "Sin destino") "Destino" else t.destination, color = Navy, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OfflineBanner(t: RobotTelemetry) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E2)), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(14.dp)) {
            Text("⚠", color = Amber, fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(if (t.stale) "TELEMETRÍA ANTIGUA" else "ROBOT DESCONECTADO", color = Amber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Último dato ${age(t.lastUpdateAt)} atrás. Las órdenes están bloqueadas.", color = Ink, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, tint: Color, modifier: Modifier, symbol: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(symbol, color = tint, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.width(6.dp))
                Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp, maxLines = 1)
            }
            Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CardBlock(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(subtitle, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun StatusPill(text: String, tint: Color) {
    Surface(color = tint.copy(alpha = 0.11f), contentColor = tint, shape = RoundedCornerShape(50)) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), maxLines = 1)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Hairline() { Spacer(Modifier.fillMaxWidth().height(1.dp).background(Line)) }

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    CardBlock {
        Text(title, color = Ink, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ScreenColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

private val activeMissionStatuses = setOf(MissionStatus.PREPARING, MissionStatus.RUNNING, MissionStatus.PAUSED, MissionStatus.BLOCKED, MissionStatus.RETURNING)

private fun MissionStatus.displayName(): String = missionLabel(this)
private fun missionLabel(status: MissionStatus): String = when (status) {
    MissionStatus.IDLE -> "Listo"
    MissionStatus.PREPARING -> "Preparando"
    MissionStatus.RUNNING -> "En ejecución"
    MissionStatus.PAUSED -> "Pausado"
    MissionStatus.BLOCKED -> "Requiere operador"
    MissionStatus.RETURNING -> "Regresando"
    MissionStatus.COMPLETED -> "Completado"
    MissionStatus.CANCELLED -> "Cancelado"
    MissionStatus.FAILED -> "Fallido"
    MissionStatus.STOPPED -> "Detenido · simulación"
}

private fun stateDescription(status: MissionStatus): String = when (status) {
    MissionStatus.IDLE -> "Esperando una misión"
    MissionStatus.COMPLETED -> "Recorrido finalizado"
    MissionStatus.CANCELLED -> "La misión fue cancelada"
    MissionStatus.STOPPED -> "Detención operativa solicitada en la demo"
    MissionStatus.FAILED -> "La misión requiere revisión"
    else -> "Revisá el estado de la misión"
}

private fun missionColor(status: MissionStatus): Color = when (status) {
    MissionStatus.RUNNING, MissionStatus.COMPLETED, MissionStatus.IDLE -> Green
    MissionStatus.BLOCKED, MissionStatus.FAILED -> Red
    MissionStatus.PAUSED, MissionStatus.RETURNING, MissionStatus.PREPARING -> Amber
    else -> Muted
}

private fun CommandType.displayName(): String = when (this) {
    CommandType.START -> "Iniciar misión"
    CommandType.PAUSE -> "Pausar misión"
    CommandType.RESUME -> "Reanudar misión"
    CommandType.CANCEL -> "Cancelar misión"
    CommandType.RETURN_SAFE -> "Regresar a punto seguro"
    CommandType.OPERATIONAL_STOP -> "Detención operativa simulada"
    CommandType.ACK_ALERT -> "Reconocer alerta"
}

private fun CommandStatus.displayName(): String = when (this) {
    CommandStatus.PENDING -> "PENDIENTE"
    CommandStatus.CONFIRMED -> "CONFIRMADA"
    CommandStatus.REJECTED -> "RECHAZADA"
    CommandStatus.NO_RESPONSE -> "SIN RESPUESTA"
}

private fun CommandStatus.color(): Color = when (this) {
    CommandStatus.PENDING -> Amber
    CommandStatus.CONFIRMED -> Green
    CommandStatus.REJECTED, CommandStatus.NO_RESPONSE -> Red
}

private fun AlertSeverity.displayName(): String = when (this) {
    AlertSeverity.INFO -> "INFORMACIÓN"
    AlertSeverity.WARNING -> "ADVERTENCIA"
    AlertSeverity.CRITICAL -> "CRÍTICA"
}

private fun AlertSeverity.color(): Color = when (this) {
    AlertSeverity.INFO -> Navy
    AlertSeverity.WARNING -> Amber
    AlertSeverity.CRITICAL -> Red
}

private fun clock(millis: Long): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(millis))
private fun age(millis: Long): String {
    val seconds = ((System.currentTimeMillis() - millis).coerceAtLeast(0) / 1000).toInt()
    return if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"
}

private fun hriMessage(t: RobotTelemetry): String = when {
    !t.connected -> "No recibí confirmación de recepción. Un operador verificará la situación."
    t.missionStatus == MissionStatus.BLOCKED -> "Hay un obstáculo. Esperá mientras recepción revisa el recorrido."
    t.missionStatus == MissionStatus.PAUSED && !t.visitorPresent -> "No puedo continuar el acompañamiento; avisaré a recepción."
    t.missionStatus == MissionStatus.PAUSED -> "Estamos detenidos. Un operador está revisando el recorrido."
    t.missionStatus == MissionStatus.RETURNING -> "Volvemos juntos a Recepción. Caminá a una distancia segura."
    t.missionStatus == MissionStatus.COMPLETED -> "Llegamos a ${t.destination}. Gracias por acompañarme."
    t.missionStatus == MissionStatus.CANCELLED || t.missionStatus == MissionStatus.STOPPED -> "El acompañamiento se detuvo. Seguí las indicaciones del operador."
    t.visitorPresent -> "Hola, te acompaño a ${t.destination}. Caminá a una distancia segura."
    else -> "Ottoman está listo para acompañar a una persona."
}
