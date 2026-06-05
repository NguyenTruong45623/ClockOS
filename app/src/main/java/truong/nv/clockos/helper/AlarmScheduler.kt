package truong.nv.clockos.helper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import truong.nv.clockos.data.models.AlarmEntity
import truong.nv.clockos.receiver.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_HOUR", alarm.hour)
            putExtra("ALARM_MINUTE", alarm.minute)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_SNOOZE_DURATION", alarm.snoozeDuration)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        // setAlarmClock guarantees delivery exactly on time even under Doze mode
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        
        Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} at $triggerTime (${Calendar.getInstance().apply { timeInMillis = triggerTime }.time})")
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        Log.d("AlarmScheduler", "Cancelled alarm $alarmId")
    }

    fun getNextTriggerTime(hour: Int, minute: Int, repeatDays: List<Int>): Long {
        val currentMillis = System.currentTimeMillis()

        if (repeatDays.isEmpty()) {
            // One-time alarm
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (calendar.timeInMillis <= currentMillis) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        } else {
            // Repeating alarm on specific days of week
            var minTriggerTime = Long.MAX_VALUE
            for (day in repeatDays) {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val calendarDay = mapRepeatDayToCalendarDay(day)
                
                val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                var daysDiff = calendarDay - currentDayOfWeek
                if (daysDiff < 0) {
                    daysDiff += 7
                }
                calendar.add(Calendar.DAY_OF_YEAR, daysDiff)
                
                // If it is today and the time has already passed, set for next week
                if (calendar.timeInMillis <= currentMillis) {
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                }
                
                if (calendar.timeInMillis < minTriggerTime) {
                    minTriggerTime = calendar.timeInMillis
                }
            }
            return minTriggerTime
        }
    }

    private fun mapRepeatDayToCalendarDay(day: Int): Int {
        return when (day) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            7 -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }
}
