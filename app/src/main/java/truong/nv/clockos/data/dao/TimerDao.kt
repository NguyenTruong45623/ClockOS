package truong.nv.clockos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import truong.nv.clockos.data.models.TimerEntity

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers ORDER BY id ASC")
    fun getTimers(): Flow<List<TimerEntity>>

    @Insert
    suspend fun insertTimer(timer: TimerEntity)

    @Delete
    suspend fun deleteTimer(timer: TimerEntity)

    @Update
    suspend fun updateTimer(timer: TimerEntity)
}