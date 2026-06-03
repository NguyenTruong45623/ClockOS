package truong.nv.clockos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import truong.nv.clockos.data.models.ActiveTimerEntity

@Dao
interface ActiveTimerDao {
    @Insert
    suspend fun insertActiveTimer(activeTimer: ActiveTimerEntity)

    @Query("SELECT * FROM active_timers ORDER BY id ASC")
    fun getActiveTimers(): Flow<List<ActiveTimerEntity>>


    @Query("DELETE FROM active_timers WHERE id = :id")
    suspend fun deleteActiveTimer(id: Long)

    @Query("UPDATE active_timers SET isRunning = :isRunning WHERE id = :id")
    suspend fun updateActiveTimer(id: Long, isRunning: Boolean)
}