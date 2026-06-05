package truong.nv.clockos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import truong.nv.clockos.service.AlarmRingingService

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val hour = intent.getIntExtra("ALARM_HOUR", 0)
        val minute = intent.getIntExtra("ALARM_MINUTE", 0)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"
        val snoozeDuration = intent.getIntExtra("ALARM_SNOOZE_DURATION", 9)

        Log.d("AlarmActionReceiver", "Action received: $action for Alarm ID: $alarmId, snooze: $snoozeDuration")

        if (alarmId == -1) return

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_SNOOZE_DURATION", snoozeDuration)
        }

        when (action) {
            "ACTION_SNOOZE" -> {
                serviceIntent.action = "ACTION_SNOOZE"
                context.startService(serviceIntent)
            }
            "ACTION_DISMISS" -> {
                serviceIntent.action = "ACTION_DISMISS"
                context.startService(serviceIntent)
            }
        }
    }
}
