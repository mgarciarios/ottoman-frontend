package ar.edu.uade.ottoman.data

import ar.edu.uade.ottoman.domain.CommandStatus
import ar.edu.uade.ottoman.domain.CommandType
import ar.edu.uade.ottoman.domain.HistoryItem
import ar.edu.uade.ottoman.domain.MissionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedOttomanRepositoryTest {
    @Test fun normalMissionMovesFromPendingToCompletedAndLogsArrival() = runBlocking {
        val repository = repository(responseDelayMs = 60)
        repository.selectDestination("Aula 204")
        val sending = launch { repository.send(CommandType.START) }
        delay(10)
        assertEquals(CommandStatus.PENDING, repository.state.value.commands.first().status)
        sending.join()
        assertEquals(CommandStatus.CONFIRMED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.PREPARING, repository.state.value.telemetry.missionStatus)

        repository.tick()
        repeat(20) { repository.tick() }

        assertEquals(MissionStatus.COMPLETED, repository.state.value.telemetry.missionStatus)
        assertEquals(100, repository.state.value.telemetry.progressPercent)
        assertEquals("Aula 204", repository.state.value.telemetry.destination)
        assertTrue(repository.state.value.history.any { it.title == "Misión completada" })
    }

    @Test fun lowBatteryRejectsNewMissionWithoutChangingMissionState() = runBlocking {
        val repository = repository()
        repository.forceLowBattery()
        repository.send(CommandType.START)

        assertEquals(CommandStatus.REJECTED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.IDLE, repository.state.value.telemetry.missionStatus)
        assertEquals(18, repository.state.value.telemetry.batteryPercent)
    }

    @Test fun obstacleMustBeResolvedBeforeResume() = runBlocking {
        val repository = repository()
        repository.forceObstacle()
        repository.send(CommandType.RESUME)
        assertEquals(CommandStatus.REJECTED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.BLOCKED, repository.state.value.telemetry.missionStatus)

        repository.resolveObstacle()
        repository.send(CommandType.RESUME)
        assertEquals(CommandStatus.CONFIRMED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.RUNNING, repository.state.value.telemetry.missionStatus)
        assertFalse(repository.state.value.alerts.first { it.type == "OBSTACLE" }.causeActive)
    }

    @Test fun visitorMustReuniteBeforeResume() = runBlocking {
        val repository = repository()
        repository.separateVisitor()
        repository.send(CommandType.RESUME)
        assertEquals(CommandStatus.REJECTED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.PAUSED, repository.state.value.telemetry.missionStatus)

        repository.reuniteVisitor()
        repository.send(CommandType.RESUME)
        assertEquals(CommandStatus.CONFIRMED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.RUNNING, repository.state.value.telemetry.missionStatus)
        assertTrue(repository.state.value.telemetry.visitorPresent)
        assertFalse(repository.state.value.alerts.first { it.type == "VISITOR_SEPARATED" }.causeActive)
    }

    @Test fun rejectedScenarioDoesNotStartMission() = runBlocking {
        val repository = repository()
        repository.rejectNextCommand()
        repository.send(CommandType.START)

        assertEquals(CommandStatus.REJECTED, repository.state.value.commands.first().status)
        assertEquals(MissionStatus.IDLE, repository.state.value.telemetry.missionStatus)
        assertTrue(repository.state.value.commands.first().reason.orEmpty().contains("escenario de prueba"))
    }

    @Test fun noResponseMarksCommandAndStaleTelemetryUntilReconnect() = runBlocking {
        var now = System.currentTimeMillis()
        val repository = repository(clock = { now }, noResponseTimeoutMs = 0)
        repository.reconnect()
        repository.simulateNoResponse()
        assertEquals(CommandStatus.NO_RESPONSE, repository.state.value.commands.first().status)
        assertFalse(repository.state.value.telemetry.connected)

        now += 6_000
        repository.tick()
        assertTrue(repository.state.value.telemetry.stale)
        repository.reconnect()
        assertFalse(repository.state.value.telemetry.stale)
        assertTrue(repository.state.value.telemetry.connected)
    }

    @Test fun acknowledgingAnAlertDoesNotResolveItsCause() = runBlocking {
        val repository = repository()
        repository.forceObstacle()
        val alert = repository.state.value.alerts.first()
        repository.acknowledge(alert.id)

        val acknowledged = repository.state.value.alerts.first { it.id == alert.id }
        assertTrue(acknowledged.acknowledged)
        assertTrue(acknowledged.causeActive)
    }

    @Test fun historyIsLoadedFromAndWrittenToTheInjectedStore() = runBlocking {
        val store = InMemoryHistoryStore()
        val first = SimulatedOttomanRepository(store, commandResponseDelayMs = 0)
        first.forceLowBattery()
        val expectedEvents = store.items

        val second = SimulatedOttomanRepository(store, commandResponseDelayMs = 0)
        assertEquals(expectedEvents, second.state.value.history)
        assertTrue(expectedEvents.isNotEmpty())
    }

    private fun repository(
        responseDelayMs: Long = 0,
        noResponseTimeoutMs: Long = 0,
        clock: () -> Long = System::currentTimeMillis,
    ) = SimulatedOttomanRepository(
        historyStore = InMemoryHistoryStore(),
        commandResponseDelayMs = responseDelayMs,
        noResponseTimeoutMs = noResponseTimeoutMs,
        clock = clock,
    )

    private class InMemoryHistoryStore : HistoryStore {
        var items: List<HistoryItem> = emptyList()
            private set

        override fun read(): List<HistoryItem> = items
        override fun write(items: List<HistoryItem>) { this.items = items.toList() }
    }
}
