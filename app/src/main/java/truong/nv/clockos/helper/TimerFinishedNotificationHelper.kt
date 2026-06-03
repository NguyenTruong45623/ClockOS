package truong.nv.clockos.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import truong.nv.clockos.MainActivity

/**
 * Helper để tạo Notification khi timer trong TimerScreen kết thúc.
 * Channel riêng, không conflict với StopTime notification.
 */
object TimerFinishedNotificationHelper {

    private const val CHANNEL_ID = "TimerFinishedChannel"
    private const val CHANNEL_NAME = "Hẹn giờ"
    private const val BASE_NOTIFICATION_ID = 2000

    /**
     * Tạo Notification Channel (gọi 1 lần khi app khởi động hoặc trước khi gửi notification).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi hẹn giờ kết thúc"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Gửi notification "Hết giờ" cho timer cụ thể.
     * @param timerId ID của timer (dùng để tạo notification ID duy nhất)
     * @param label Nhãn của timer (vd: "5 min", "Luộc trứng")
     */
    fun showFinishedNotification(context: Context, timerId: String, label: String) {
        createNotificationChannel(context)

        // PendingIntent mở lại app khi bấm vào notification
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context,
            timerId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ Hết giờ!")
            .setContentText("Hẹn giờ \"$label\" đã kết thúc")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(tapPending)
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = BASE_NOTIFICATION_ID + (timerId.hashCode() and 0x7FFFFFFF) % 1000
        manager.notify(notificationId, notification)
    }

    /**
     * Rung thiết bị khi timer kết thúc.
     */
    fun vibrateDevice(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1)
            )
        }
    }

    /**
     * Phát âm thanh báo thức.
     */
    fun playAlarmSound(context: Context) {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
