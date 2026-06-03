package truong.nv.clockos.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import truong.nv.clockos.data.models.StopWatchEntity

@Dao
interface StopWatchDao {

    @Insert
    suspend fun insertStopwatch(stopwatch: StopWatchEntity)

    @Delete
    suspend fun deleteStopwatch(stopwatch: StopWatchEntity)

    @Update
    suspend fun updateStopwatch(stopwatch: StopWatchEntity)

    @Query("SELECT * FROM stopwatch ORDER BY id ASC")
    fun getStopwatches(): Flow<List<StopWatchEntity>>
}