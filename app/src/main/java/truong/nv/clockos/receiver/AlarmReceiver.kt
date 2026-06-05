package truong.nv.clockos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import truong.nv.clockos.service.AlarmRingingService
import truong.nv.clockos.ui.feature.alarm.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val hour = intent.getIntExtra("ALARM_HOUR", 0)
        val minute = intent.getIntExtra("ALARM_MINUTE", 0)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"
        val snoozeDuration = intent.getIntExtra("ALARM_SNOOZE_DURATION", 9)

        Log.d("AlarmReceiver", "Alarm triggered broadcast received! ID: $alarmId, time: $hour:$minute, label: $label, snooze: $snoozeDuration")

        if (alarmId == -1) return

        // 1. Start Foreground Service to play sound, vibration and show heads-up notification
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = "ACTION_RING"
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_SNOOZE_DURATION", snoozeDuration)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Launch fullscreen overlay activity (designed for lockscreen compatibility)
        val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_SNOOZE_DURATION", snoozeDuration)
        }
        context.startActivity(activityIntent)
    }
}
