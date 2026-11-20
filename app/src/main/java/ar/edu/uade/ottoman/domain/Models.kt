package ar.edu.uade.ottoman.domain

enum class MissionStatus { IDLE, PREPARING, RUNNING, PAUSED, BLOCKED, RETURNING, COMPLETED, CANCELLED, FAILED, STOPPED }
enum class CommandType { START, PAUSE, RESUME, CANCEL, RETURN_SAFE, OPERATIONAL_STOP, ACK_ALERT }
enum class CommandStatus { PENDING, CONFIRMED, REJECTED, NO_RESPONSE }
enum class AlertSeverity { INFO, WARNING, CRITICAL }

data class RobotAlert(
    val id: String,
    val severity: AlertSeverity,
    val type: String,
    val message: String,
    val createdAt: Long,
    val acknowledged: Boolean = false,
    val causeActive: Boolean = true,
)

data class RobotCommand(
    val id: String,
    val type: CommandType,
    val status: CommandStatus,
    val createdAt: Long,
    val reason: String? = null,
)

data class HistoryItem(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: Long,
)

data class RobotTelemetry(
    val connected: Boolean = true,
    val stale: Boolean = false,
    val lastUpdateAt: Long = System.currentTimeMillis(),
    val batteryPercent: Int = 82,
    val missionStatus: MissionStatus = MissionStatus.IDLE,
    val destination: String = "Sin destino",
    val progressPercent: Int = 0,
    val waypoint: String = "Recepción",
    val waypointIndex: Int = 0,
    val waypointCount: Int = 4,
    val speedMetersPerSecond: Double = 0.0,
    val visitorPresent: Boolean = false,
    val missionId: String? = null,
)

data class OttomanUiState(
    val telemetry: RobotTelemetry = RobotTelemetry(),
    val alerts: List<RobotAlert> = emptyList(),
    val commands: List<RobotCommand> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val selectedDestination: String = "Aula Magna",
    val rejectNextCommand: Boolean = false,
)
