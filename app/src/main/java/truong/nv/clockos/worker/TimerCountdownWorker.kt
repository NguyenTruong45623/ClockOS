package truong.nv.clockos.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import truong.nv.clockos.helper.TimerFinishedNotificationHelper

/**
 * Worker chạy khi timer hết giờ.
 * Được WorkManager gọi sau khoảng delay = tổng thời gian đếm ngược.
 *
 * Công việc:
 * 1. Gửi notification "Hết giờ"
 * 2. Phát âm thanh báo thức
 * 3. Rung thiết bị
 */
@HiltWorker
class TimerCountdownWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TIMER_ID = "timer_id"
        const val KEY_TIMER_LABEL = "timer_label"
        const val WORK_NAME_PREFIX = "timer_countdown_"
    }

    override suspend fun doWork(): Result {
        val timerId = inputData.getString(KEY_TIMER_ID) ?: return Result.failure()
        val label = inputData.getString(KEY_TIMER_LABEL) ?: "Hẹn giờ"

        // Gửi notification
        TimerFinishedNotificationHelper.showFinishedNotification(context, timerId, label)

        // Phát âm thanh + rung
        TimerFinishedNotificationHelper.playAlarmSound(context)
        TimerFinishedNotificationHelper.vibrateDevice(context)

        return Result.success()
    }
}
