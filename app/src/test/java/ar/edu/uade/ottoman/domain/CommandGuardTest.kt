package ar.edu.uade.ottoman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommandGuardTest {
    @Test fun startRequiresConnectedRobotAndEnoughBattery() {
        assertEquals("Batería insuficiente para iniciar (mínimo 25%).", CommandGuard.rejectionReason(CommandType.START, OttomanUiState(telemetry = RobotTelemetry(batteryPercent = 18))))
        assertEquals("Robot desconectado; la orden no se envió.", CommandGuard.rejectionReason(CommandType.START, OttomanUiState(telemetry = RobotTelemetry(connected = false))))
        assertNull(CommandGuard.rejectionReason(CommandType.START, OttomanUiState()))
    }

    @Test fun pauseOnlyWorksDuringMotion() {
        assertEquals("No hay misión en movimiento para pausar.", CommandGuard.rejectionReason(CommandType.PAUSE, OttomanUiState()))
        assertNull(CommandGuard.rejectionReason(CommandType.PAUSE, OttomanUiState(telemetry = RobotTelemetry(missionStatus = MissionStatus.RUNNING))))
    }

    @Test fun resumeWaitsForObstacleAndVisitorResolution() {
        val blocked = OttomanUiState(
            telemetry = RobotTelemetry(missionStatus = MissionStatus.BLOCKED),
            alerts = listOf(RobotAlert("a1", AlertSeverity.CRITICAL, "OBSTACLE", "Obstáculo", 1L)),
        )
        assertEquals("Primero hay que resolver el obstáculo.", CommandGuard.rejectionReason(CommandType.RESUME, blocked))
        val separated = OttomanUiState(
            telemetry = RobotTelemetry(missionStatus = MissionStatus.PAUSED),
            alerts = listOf(RobotAlert("a2", AlertSeverity.CRITICAL, "VISITOR_SEPARATED", "Visitante separado", 1L)),
        )
        assertEquals("Esperá a que el visitante vuelva al acompañamiento.", CommandGuard.rejectionReason(CommandType.RESUME, separated))
        assertNull(CommandGuard.rejectionReason(CommandType.RESUME, OttomanUiState(telemetry = RobotTelemetry(missionStatus = MissionStatus.PAUSED))))
    }

    @Test fun resumeRequiresBatteryThreshold() {
        val pausedWithLowBattery = OttomanUiState(telemetry = RobotTelemetry(missionStatus = MissionStatus.PAUSED, batteryPercent = 18))
        assertEquals("Batería insuficiente para reanudar.", CommandGuard.rejectionReason(CommandType.RESUME, pausedWithLowBattery))
    }

    @Test fun returnToSafePointNeedsActiveMissionAndNoncriticalBattery() {
        assertEquals("No hay un recorrido para regresar.", CommandGuard.rejectionReason(CommandType.RETURN_SAFE, OttomanUiState()))
        assertEquals("Batería crítica; requiere intervención del operador.", CommandGuard.rejectionReason(CommandType.RETURN_SAFE, OttomanUiState(telemetry = RobotTelemetry(missionStatus = MissionStatus.RUNNING, batteryPercent = 8))))
        assertNull(CommandGuard.rejectionReason(CommandType.RETURN_SAFE, OttomanUiState(telemetry = RobotTelemetry(missionStatus = MissionStatus.RUNNING, batteryPercent = 18))))
    }

    @Test fun acknowledgingAlertIsLocalAndAllowedOffline() {
        assertNull(CommandGuard.rejectionReason(CommandType.ACK_ALERT, OttomanUiState(telemetry = RobotTelemetry(connected = false))))
    }
}
