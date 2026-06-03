package truong.nv.clockos

import android.app.Application
import com.chibatching.kotpref.Kotpref
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
    }
}