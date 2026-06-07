package truong.nv.clockos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import truong.nv.clockos.service.AlarmRingingService

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getIntExtra(AlarmRingingService.ALARM_ID, -1)
        val hour = intent.getIntExtra(AlarmRingingService.ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AlarmRingingService.ALARM_MINUTE, 0)
        val label = intent.getStringExtra(AlarmRingingService.ALARM_LABEL) ?: "Báo thức"
        val snoozeDuration = intent.getIntExtra(AlarmRingingService.ALARM_SNOOZE_DURATION, 9)

        Log.d("AlarmActionReceiver", "Action received: $action for Alarm ID: $alarmId, snooze: $snoozeDuration")

        if (alarmId == -1) return

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(AlarmRingingService.ALARM_ID, alarmId)
            putExtra(AlarmRingingService.ALARM_HOUR, hour)
            putExtra(AlarmRingingService.ALARM_MINUTE, minute)
            putExtra(AlarmRingingService.ALARM_LABEL, label)
            putExtra(AlarmRingingService.ALARM_SNOOZE_DURATION, snoozeDuration)
        }

        when (action) {
            AlarmRingingService.ACTION_SNOOZE -> {
                serviceIntent.action = AlarmRingingService.ACTION_SNOOZE
                context.startService(serviceIntent)
            }
            AlarmRingingService.ACTION_DISMISS -> {
                serviceIntent.action = AlarmRingingService.ACTION_DISMISS
                context.startService(serviceIntent)
            }
        }
    }
}
