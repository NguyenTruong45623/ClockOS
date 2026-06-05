package truong.nv.clockos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import truong.nv.clockos.data.dao.ActiveTimerDao
import truong.nv.clockos.data.dao.AlarmDao
import truong.nv.clockos.data.dao.StopWatchDao
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.models.ActiveTimerEntity
import truong.nv.clockos.data.models.AlarmEntity
import truong.nv.clockos.data.models.Converters
import truong.nv.clockos.data.models.StopWatchEntity
import truong.nv.clockos.data.models.TimerEntity

@Database(
    entities = [
        TimerEntity::class,
        StopWatchEntity::class,
        ActiveTimerEntity::class,
        AlarmEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
    abstract fun stopWatchDao(): StopWatchDao
    abstract fun activeTimerDao(): ActiveTimerDao
    abstract fun alarmDao(): AlarmDao
}