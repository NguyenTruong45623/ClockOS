package truong.nv.clockos.data.repository

import kotlinx.coroutines.flow.Flow
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.models.TimerEntity

class TimerRepositoryImpl(
    private val timerDao: TimerDao
) : TimerRepository {
    override fun getTimers(): Flow<List<TimerEntity>> {
        return timerDao.getTimers()
    }

    override suspend fun insertTimer(timer: TimerEntity) {
        timerDao.insertTimer(timer)
    }

    override suspend fun deleteTimer(timer: TimerEntity) {
        timerDao.deleteTimer(timer)
    }

    override suspend fun updateTimer(timer: TimerEntity) {
        timerDao.updateTimer(timer)
    }

}