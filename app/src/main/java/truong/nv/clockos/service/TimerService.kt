package truong.nv.clockos.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import truong.nv.clockos.helper.AlarmHelper
import truong.nv.clockos.helper.TimerNotificationHelper

/**
 * Trạng thái của bộ đếm ngược, dùng để UI observe.
 */
data class StopTimeState(
    val timeLeftInMillis: Long = 0L,
    val totalTimeInMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

class TimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_CANCEL = "ACTION_CANCEL"
        const val ACTION_TIME_UP = "ACTION_TIME_UP"
        const val EXTRA_TIME_IN_MS = "EXTRA_TIME_IN_MS"

        // State chia sẻ giữa Service và UI (Compose observe được)
        private val _state = MutableStateFlow(StopTimeState())
        val state: StateFlow<StopTimeState> = _state.asStateFlow()
    }

    private var timeLeftInMillis = 0L
    private var totalTimeInMillis = 0L
    private var endTime = 0L
    private var isRunning = false
    private var ringtone: android.media.Ringtone? = null

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Notification Channel
        TimerNotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val time = intent.getLongExtra(EXTRA_TIME_IN_MS, 0L)
                if (time > 0) {
                    totalTimeInMillis = time
                    timeLeftInMillis = time
                    endTime = System.currentTimeMillis() + timeLeftInMillis
                    isRunning = true

                    // Đặt AlarmManager để kêu khi hết giờ
                    AlarmHelper.setTimer(this, timeLeftInMillis)

                    // Bắt đầu foreground service với notification
                    val notification = TimerNotificationHelper.buildNotification(this, isRunning, timeLeftInMillis, endTime, isFinished = false)
                    startForegroundCompat(notification)

                    // Bắt đầu ticker cập nhật UI mỗi giây
                    startTicker()

                    // Phát state về UI
                    emitState()
                }
            }
            ACTION_PAUSE -> {
                if (isRunning) {
                    timeLeftInMillis = endTime - System.currentTimeMillis()
                    if (timeLeftInMillis < 0) timeLeftInMillis = 0
                    isRunning = false

                    // Hủy AlarmManager khi tạm dừng
                    AlarmHelper.cancelTimer(this)

                    // Dừng ticker
                    tickerJob?.cancel()

                    updateNotification()
                    emitState()
                }
            }
            ACTION_RESUME -> {
                if (!isRunning && timeLeftInMillis > 0) {
                    endTime = System.currentTimeMillis() + timeLeftInMillis
                    isRunning = true

                    // Đặt lại AlarmManager
                    AlarmHelper.setTimer(this, timeLeftInMillis)

                    // Khởi động lại ticker
                    startTicker()

                    updateNotification()
                    emitState()
                }
            }
            ACTION_CANCEL -> {
                // Hủy AlarmManager
                AlarmHelper.cancelTimer(this)

                // Dừng ticker
                tickerJob?.cancel()

                // Dừng nhạc và rung (nếu đang kêu)
                stopAlarmSoundAndVibration()

                // Reset state
                isRunning = false
                timeLeftInMillis = 0
                totalTimeInMillis = 0

                _state.value = StopTimeState()

                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_TIME_UP -> {
                // Hết giờ! Phát âm thanh + rung
                isRunning = false
                timeLeftInMillis = 0
                tickerJob?.cancel()

                _state.value = StopTimeState(
                    timeLeftInMillis = 0,
                    totalTimeInMillis = totalTimeInMillis,
                    isRunning = false,
                    isFinished = true
                )

                // Bắt buộc gọi startForeground vì TimerReceiver gọi startForegroundService()
                // Nếu không gọi trong 5 giây → app crash (ForegroundServiceDidNotStartInTimeException)
                val notification = TimerNotificationHelper.buildNotification(this, false, 0, 0, isFinished = true)
                startForegroundCompat(notification)

                // Rung thiết bị
                vibrateDevice()

                // Phát âm thanh báo thức mặc định
                playAlarmSound()
            }
        }
        return START_NOT_STICKY
    }

    /**
     * Ticker chạy mỗi giây, cập nhật timeLeft và phát state mới cho UI.
     */
    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val now = System.currentTimeMillis()
                timeLeftInMillis = endTime - now
                if (timeLeftInMillis <= 0) {
                    timeLeftInMillis = 0
                    isRunning = false
                    emitState()
                    break
                }
                emitState()
                // Cập nhật notification
                withContext(Dispatchers.Main) {
                    updateNotification()
                }
            }
        }
    }

    private fun emitState() {
        _state.value = StopTimeState(
            timeLeftInMillis = timeLeftInMillis,
            totalTimeInMillis = totalTimeInMillis,
            isRunning = isRunning,
            isFinished = false
        )
    }

    private fun updateNotification(isFinished: Boolean = false) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            TimerNotificationHelper.NOTIFICATION_ID,
            TimerNotificationHelper.buildNotification(this, isRunning, timeLeftInMillis, endTime, isFinished)
        )
    }

    private fun vibrateDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), 0))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), 0))
        }
    }

    private fun playAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSoundAndVibration() {
        try {
            ringtone?.stop()
            ringtone = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Wrapper cho startForeground() tương thích với Android 14+ (API 34).
     * API 34 yêu cầu truyền foregroundServiceType, nếu thiếu → MissingForegroundServiceTypeException.
     */
    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                TimerNotificationHelper.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                TimerNotificationHelper.NOTIFICATION_ID,
                notification
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSoundAndVibration()
        tickerJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}