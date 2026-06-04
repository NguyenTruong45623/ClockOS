package truong.nv.clockos.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import truong.nv.clockos.helper.TimerFinishedNotificationHelper

class StopAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_STOP_ALARM") {
            // Tắt âm thanh và rung
            TimerFinishedNotificationHelper.stopAlarmSoundAndVibration(context)

            // Hủy Notification hiển thị "Hết giờ"
            val timerId = intent.getStringExtra("TIMER_ID") ?: return
            val notificationId = 2000 + (timerId.hashCode() and 0x7FFFFFFF) % 1000
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(notificationId)
        }
    }
}
