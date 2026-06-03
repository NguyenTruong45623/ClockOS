package truong.nv.clockos.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stopwatch")
data class StopWatchEntity(
    @PrimaryKey (autoGenerate = true)
    val id: Int = 0,

    val elapsedTime: Long = 0L,

    val startTimestamp: Long = 0L,
)