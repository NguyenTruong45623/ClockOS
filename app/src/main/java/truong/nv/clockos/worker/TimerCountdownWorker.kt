package truong.nv.clockos.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import truong.nv.clockos.helper.TimerFinishedNotificationHelper

/**
 * Worker chạy Foreground để đếm ngược timer.
 * Không bị kill khi thoát app.
 */
@HiltWorker
class TimerCountdownWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TIMER_ID = "timer_id"
        const val KEY_TIMER_LABEL = "timer_label"
        const val KEY_TOTAL_SECONDS = "total_seconds"
        const val KEY_REMAINING_SECONDS = "remaining_seconds" // Progress output
        const val WORK_NAME_PREFIX = "timer_countdown_"
        const val WORK_TAG_TIMER = "TIMER_WORKER_TAG"
    }

    override suspend fun doWork(): Result {
        val timerId = inputData.getString(KEY_TIMER_ID) ?: return Result.failure()
        val label = inputData.getString(KEY_TIMER_LABEL) ?: "Hẹn giờ"
        var remainingSeconds = inputData.getLong(KEY_TOTAL_SECONDS, 0L)
        
        if (remainingSeconds <= 0) return Result.failure()

        val notificationId = (TimerFinishedNotificationHelper.hashCode() and 0x7FFFFFFF) % 1000 + timerId.hashCode() % 1000

        // Set as Foreground Service
        val foregroundInfo = createForegroundInfo(notificationId, timerId, label, remainingSeconds)
        setForeground(foregroundInfo)

        // Bắt đầu đếm ngược
        while (remainingSeconds > 0) {
            if (isStopped) {
                // Hủy Worker
                return Result.success()
            }
            
            delay(1000L)
            remainingSeconds--

            // Cập nhật Notification
            setForeground(createForegroundInfo(notificationId, timerId, label, remainingSeconds))
            
            // Cập nhật Progress cho ViewModel (nếu đang mở app)
            setProgress(workDataOf(KEY_REMAINING_SECONDS to remainingSeconds))
        }

        // Đếm xong, gọi báo thức
        TimerFinishedNotificationHelper.showFinishedNotification(context, timerId, label)
        TimerFinishedNotificationHelper.playAlarmSound(context)
        TimerFinishedNotificationHelper.vibrateDevice(context)

        return Result.success()
    }

    private fun createForegroundInfo(notificationId: Int, timerId: String, label: String, remainingSeconds: Long): ForegroundInfo {
        val notification = TimerFinishedNotificationHelper.createForegroundNotification(context, timerId, label, remainingSeconds)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}
