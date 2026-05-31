package truong.nv.clockos.data.models

data class TimerItem(
    val id: String,
    val label: String,
    val totalSeconds: Long,
    var remainingSeconds: Long,
    var isRunning: Boolean
)