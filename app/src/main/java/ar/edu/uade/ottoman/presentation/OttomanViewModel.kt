package ar.edu.uade.ottoman.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.uade.ottoman.data.SimulatedOttomanRepository
import ar.edu.uade.ottoman.data.SharedPreferencesHistoryStore
import ar.edu.uade.ottoman.domain.CommandType
import ar.edu.uade.ottoman.domain.OttomanRepository
import ar.edu.uade.ottoman.domain.OttomanUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OttomanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OttomanRepository = SimulatedOttomanRepository(SharedPreferencesHistoryStore(application))
    val state: StateFlow<OttomanUiState> = repository.state

    init {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                repository.tick()
            }
        }
    }

    fun selectDestination(destination: String) = launch { repository.selectDestination(destination) }
    fun send(type: CommandType) = launch { repository.send(type) }
    fun acknowledge(alertId: String) = launch { repository.acknowledge(alertId) }
    fun disconnect() = launch { repository.disconnect() }
    fun reconnect() = launch { repository.reconnect() }
    fun lowBattery() = launch { repository.forceLowBattery() }
    fun obstacle() = launch { repository.forceObstacle() }
    fun resolveObstacle() = launch { repository.resolveObstacle() }
    fun separateVisitor() = launch { repository.separateVisitor() }
    fun reuniteVisitor() = launch { repository.reuniteVisitor() }
    fun rejectNext() = launch { repository.rejectNextCommand() }
    fun noResponse() = launch { repository.simulateNoResponse() }
    fun reset() = launch { repository.resetRobot() }
    fun normalDemo() = launch {
        repository.resetRobot()
        repository.selectDestination("Aula Magna")
        repository.send(CommandType.START)
    }

    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
