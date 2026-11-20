package ar.edu.uade.ottoman.domain

import kotlinx.coroutines.flow.StateFlow

interface OttomanRepository {
    val state: StateFlow<OttomanUiState>
    suspend fun tick()
    suspend fun selectDestination(destination: String)
    suspend fun send(type: CommandType)
    suspend fun acknowledge(alertId: String)
    suspend fun disconnect()
    suspend fun reconnect()
    suspend fun forceLowBattery()
    suspend fun forceObstacle()
    suspend fun resolveObstacle()
    suspend fun separateVisitor()
    suspend fun reuniteVisitor()
    suspend fun rejectNextCommand()
    suspend fun simulateNoResponse()
    suspend fun resetRobot()
}
