package truong.nv.clockos.data.repository

import kotlinx.coroutines.flow.Flow
import truong.nv.clockos.data.models.TimerEntity

interface TimerRepository {
    fun getTimers(): Flow<List<TimerEntity>>
    suspend fun insertTimer(timer: TimerEntity)
    suspend fun deleteTimer(timer: TimerEntity)
    suspend fun updateTimer(timer: TimerEntity)
}
