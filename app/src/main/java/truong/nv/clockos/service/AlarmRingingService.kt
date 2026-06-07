package truong.nv.clockos.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import truong.nv.clockos.data.dao.AlarmDao
import truong.nv.clockos.helper.AlarmScheduler
import truong.nv.clockos.receiver.AlarmActionReceiver
import truong.nv.clockos.receiver.AlarmReceiver

import javax.inject.Inject

@AndroidEntryPoint
class AlarmRingingService : Service() {

    companion object {
        const val CHANNEL_ID = "AlarmRingingChannel"
        const val NOTIFICATION_ID = 222
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val ACTION_RING = "ACTION_RING"
        const val ALARM_ID = "ALARM_ID"
        const val ALARM_HOUR = "ALARM_HOUR"
        const val ALARM_MINUTE = "ALARM_MINUTE"
        const val ALARM_LABEL = "ALARM_LABEL"
        const val ALARM_SNOOZE_DURATION = "ALARM_SNOOZE_DURATION"
    }

    @Inject
    lateinit var alarmDao: AlarmDao

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoDismissJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getIntExtra(ALARM_ID, -1) ?: -1
        val hour = intent?.getIntExtra(ALARM_HOUR, 0) ?: 0
        val minute = intent?.getIntExtra(ALARM_MINUTE, 0) ?: 0
        val label = intent?.getStringExtra(ALARM_LABEL) ?: "Báo thức"
        val snoozeDuration = intent?.getIntExtra(ALARM_SNOOZE_DURATION, 9) ?: 9

        Log.d("AlarmRingingService", "Service action: $action for Alarm ID: $alarmId, snooze: $snoozeDuration")

        if (alarmId == -1) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (action) {
            ACTION_RING -> {
                startRinging(alarmId, hour, minute, label, snoozeDuration)
            }
            ACTION_SNOOZE -> {
                snoozeAlarm(alarmId, hour, minute, label, snoozeDuration)
            }
            ACTION_DISMISS -> {
                dismissAlarm(alarmId)
            }
            else -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startRinging(alarmId: Int, hour: Int, minute: Int, label: String, snoozeDuration: Int) {
        // 1. Play sound
        playAlarmSound()

        // 2. Vibrate
        startVibration()

        // 3. Build & Show notification with full-screen intent
        val notification = buildRingingNotification(alarmId, hour, minute, label, snoozeDuration)
        startForegroundCompat(notification)

        // 4. Auto-dismiss after 5 minutes of continuous ringing to save battery
        autoDismissJob?.cancel()
        autoDismissJob = serviceScope.launch {
            delay(5 * 60 * 1000L) // 5 minutes
            withContext(Dispatchers.Main) {
                Log.d("AlarmRingingService", "Auto-snoozing alarm $alarmId after timeout")
                snoozeAlarm(alarmId, hour, minute, label, snoozeDuration)
            }
        }
    }

    private fun snoozeAlarm(alarmId: Int, hour: Int, minute: Int, label: String, snoozeDuration: Int) {
        stopAlarmSoundAndVibration()
        autoDismissJob?.cancel()

        // Schedule snooze alarm
        val snoozeTime = System.currentTimeMillis() + snoozeDuration * 60 * 1000L
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(ALARM_ID, alarmId)
            putExtra(ALARM_HOUR, hour)
            putExtra(ALARM_MINUTE, minute)
            putExtra(ALARM_LABEL, "$label (Snooze)")
            putExtra(ALARM_SNOOZE_DURATION, snoozeDuration)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId + 100000, // Unique request code offset for snooze
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmClockInfo = AlarmManager.AlarmClockInfo(snoozeTime, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        Log.d("AlarmRingingService", "Alarm $alarmId snoozed. Next ring in $snoozeDuration minutes.")
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun dismissAlarm(alarmId: Int) {
        stopAlarmSoundAndVibration()
        autoDismissJob?.cancel()

        serviceScope.launch {
            val alarm = alarmDao.getAlarmById(alarmId)
            if (alarm != null) {
                if (alarm.repeatDays.isNotEmpty()) {
                    // Reschedule the next normal repeating alarm
                    AlarmScheduler.scheduleAlarm(applicationContext, alarm)
                    Log.d("AlarmRingingService", "Rescheduled repeating alarm $alarmId")
                } else {
                    // One-time alarm: disable it
                    alarmDao.updateAlarm(alarm.copy(isEnabled = false))
                    Log.d("AlarmRingingService", "Disabled one-time alarm $alarmId")
                }
            }
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun playAlarmSound() {
        if (mediaPlayer != null) return
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to play alarm sound", e)
        }
    }

    private fun startVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            vibrator?.let {
                val pattern = longArrayOf(0, 800, 800, 800) // Vibrate 800ms, sleep 800ms
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createWaveform(pattern, 0)) // Repeat from index 0
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to start vibration", e)
        }
    }

    private fun stopAlarmSoundAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to stop media player", e)
        }
        
        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            Log.e("AlarmRingingService", "Failed to stop vibration", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lịch báo thức đang kêu",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh dùng để hiển thị màn hình báo thức và chuông báo thức đang hoạt động."
                enableLights(true)
                enableVibration(false) // Handle vibration manually in code for higher reliability
                setSound(null, null)   // Sound handled manually via MediaPlayer
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildRingingNotification(alarmId: Int, hour: Int, minute: Int, label: String, snoozeDuration: Int): Notification {

        // 2. Action buttons PendingIntents
        val snoozeIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(ALARM_HOUR, hour)
            putExtra(ALARM_MINUTE, minute)
            putExtra(ALARM_LABEL, label)
            putExtra(ALARM_SNOOZE_DURATION, snoozeDuration)
        }
        val snoozePending = PendingIntent.getBroadcast(
            this,
            alarmId + 200000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra(ALARM_ID, alarmId)
            putExtra(ALARM_HOUR, hour)
            putExtra(ALARM_MINUTE, minute)
            putExtra(ALARM_LABEL, label)
            putExtra(ALARM_SNOOZE_DURATION, snoozeDuration)
        }
        val dismissPending = PendingIntent.getBroadcast(
            this,
            alarmId + 300000,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText(String.format("%02d:%02d", hour, minute))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_pause, "Lặp lại (Snooze)", snoozePending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tắt (Dismiss)", dismissPending)
            .build()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSoundAndVibration()
        autoDismissJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
