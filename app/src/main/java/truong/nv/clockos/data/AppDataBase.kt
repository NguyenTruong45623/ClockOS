package truong.nv.clockos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.models.TimerEntity

@Database(entities = [TimerEntity::class], version = 1,exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun timerDao(): TimerDao
}