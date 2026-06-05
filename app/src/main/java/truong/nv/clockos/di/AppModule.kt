package truong.nv.clockos.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import truong.nv.clockos.data.AppDataBase
import truong.nv.clockos.data.dao.StopWatchDao
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.dao.AlarmDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataBase(
        @ApplicationContext context: Context
    ): AppDataBase {
        return Room.databaseBuilder(
            context,
            AppDataBase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideTimerDao(
        appDataBase: AppDataBase
    ): TimerDao {
        return appDataBase.timerDao()
    }

    @Provides
    fun provideStopWatchDao(
        appDataBase: AppDataBase
    ): StopWatchDao {
        return appDataBase.stopWatchDao()
    }

    @Provides
    fun provideAlarmDao(
        appDataBase: AppDataBase
    ): AlarmDao {
        return appDataBase.alarmDao()
    }

}