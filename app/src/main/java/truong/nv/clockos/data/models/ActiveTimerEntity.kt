package truong.nv.clockos.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_timers")
data class ActiveTimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timerId: Long,
    val label: String,
    val time: Long,
    val sound: String,
    val isRunning: Boolean,
    val remainingTime: Long,
)