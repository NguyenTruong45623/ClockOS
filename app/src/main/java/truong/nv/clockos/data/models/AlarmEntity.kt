package truong.nv.clockos.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Báo thức",
    val isEnabled: Boolean = true,
    val repeatDays: List<Int> = emptyList(), // 1 = Thứ hai, 2 = Thứ ba, ..., 7 = Chủ nhật
    val isSnoozeEnabled: Boolean = true,
    val snoozeDuration: Int = 9,
    val soundName: String = "Mặc định"
)
