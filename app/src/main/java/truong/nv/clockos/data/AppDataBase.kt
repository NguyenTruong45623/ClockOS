package truong.nv.clockos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import truong.nv.clockos.data.dao.ActiveTimerDao
import truong.nv.clockos.data.dao.StopWatchDao
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.models.ActiveTimerEntity
import truong.nv.clockos.data.models.StopWatchEntity
import truong.nv.clockos.data.models.TimerEntity

@Database(entities = [TimerEntity::class, StopWatchEntity::class, ActiveTimerEntity::class], version = 1,exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
    abstract fun stopWatchDao(): StopWatchDao
    abstract fun activeTimerDao(): ActiveTimerDao
}