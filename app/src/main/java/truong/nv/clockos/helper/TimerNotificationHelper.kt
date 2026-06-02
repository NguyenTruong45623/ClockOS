package truong.nv.clockos.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import truong.nv.clockos.MainActivity
import truong.nv.clockos.service.TimerService

object TimerNotificationHelper {

    const val CHANNEL_ID = "TimerChannel"
    const val NOTIFICATION_ID = 111

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer Background",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        isRunning: Boolean,
        timeLeftInMillis: Long,
        endTime: Long,
        isFinished: Boolean = false
    ): Notification {

        // PendingIntent mở lại app khi bấm vào notification
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val tapPending = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(tapPending)

        // Nút HỦY (Dùng chung cho tất cả trạng thái)
        val cancelIntent = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_CANCEL }
        val cancelPending = PendingIntent.getService(context, 2, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        when {
            // ========== HẾT GIỜ ==========
            isFinished -> {
                builder.setContentTitle("⏰ Hết giờ!")
                    .setContentText("Đếm ngược đã kết thúc")
                    .setUsesChronometer(false)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tắt", cancelPending)
            }

            // ========== ĐANG CHẠY ==========
            isRunning -> {
                val pauseIntent = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_PAUSE }
                val pausePending = PendingIntent.getService(context, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                builder.setContentTitle("Đếm ngược")
                    .setContentText(formatTime(timeLeftInMillis))
                    .setUsesChronometer(true)
                    .setWhen(endTime)
                    .setShowWhen(true)
                    .addAction(android.R.drawable.ic_media_pause, "Tạm dừng", pausePending)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hủy", cancelPending)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setChronometerCountDown(true)
                }
            }

            // ========== TẠM DỪNG ==========
            else -> {
                val resumeIntent = Intent(context, TimerService::class.java).apply { action = TimerService.ACTION_RESUME }
                val resumePending = PendingIntent.getService(context, 3, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                builder.setContentTitle("Đã tạm dừng")
                    .setUsesChronometer(false)
                    .setShowWhen(false)
                    .setContentText(formatTime(timeLeftInMillis))
                    .addAction(android.R.drawable.ic_media_play, "Tiếp tục", resumePending)
                    .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hủy", cancelPending)
            }
        }

        return builder.build()
    }

    private fun formatTime(timeInMillis: Long): String {
        val totalSeconds = timeInMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}