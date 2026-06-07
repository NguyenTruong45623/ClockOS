package truong.nv.clockos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import truong.nv.clockos.service.AlarmRingingService


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmRingingService.ALARM_ID, -1)
        val hour = intent.getIntExtra(AlarmRingingService.ALARM_HOUR, 0)
        val minute = intent.getIntExtra(AlarmRingingService.ALARM_MINUTE, 0)
        val label = intent.getStringExtra(AlarmRingingService.ALARM_LABEL) ?: "Báo thức"
        val snoozeDuration = intent.getIntExtra(AlarmRingingService.ALARM_SNOOZE_DURATION, 9)

        Log.d("AlarmReceiver", "Alarm triggered broadcast received! ID: $alarmId, time: $hour:$minute, label: $label, snooze: $snoozeDuration")

        if (alarmId == -1) return

        // 1. Start Foreground Service to play sound, vibration and show heads-up notification
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_RING
            putExtra(AlarmRingingService.ALARM_ID, alarmId)
            putExtra(AlarmRingingService.ALARM_HOUR, hour)
            putExtra(AlarmRingingService.ALARM_MINUTE, minute)
            putExtra(AlarmRingingService.ALARM_LABEL, label)
            putExtra(AlarmRingingService.ALARM_SNOOZE_DURATION, snoozeDuration)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }


    }

}
