package truong.nv.clockos.data

import com.chibatching.kotpref.KotprefModel

object StopWatchPref : KotprefModel() {

    var isRunning by booleanPref(default = false)

    var elapsedTime by longPref(default = 0L)

    var pauseTimestamp by longPref(default = 0L)

    var startTimestamp by longPref(default = 0L)
}