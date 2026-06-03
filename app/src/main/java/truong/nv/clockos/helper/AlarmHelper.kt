package truong.nv.clockos.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import truong.nv.clockos.receiver.TimerReceiver

object AlarmHelper {

    fun setTimer(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerReceiver::class.java)

        // SỬA Ở ĐÂY: Dùng getBroadcast thay vì getReceiver
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + timeInMillis

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelTimer(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerReceiver::class.java)

        // SỬA Ở ĐÂY: Dùng getBroadcast thay vì getReceiver
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}