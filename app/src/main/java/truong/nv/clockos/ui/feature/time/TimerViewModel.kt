package truong.nv.clockos.ui.feature.time

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import truong.nv.clockos.data.dao.TimerDao
import truong.nv.clockos.data.models.TimerItem
import truong.nv.clockos.worker.TimerCountdownWorker
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class TimerNavigationState {
    LIST,   // Màn hình danh sách (Hình bạn vừa gửi)
    SETUP,  // Màn hình cuộn chọn bánh xe để thêm mới
    ACTIVE  // Màn hình đếm ngược vòng tròn lớn
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerDao,
    private val application: Application
) : ViewModel() {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val workManager = WorkManager.getInstance(application)

    init {
        viewModelScope.launch {
            repository.getTimers().collect { items ->

            }
        }
        // Cài đặt dữ liệu mẫu ban đầu (Mock Data)
        _uiState.update {
            it.copy(
                timerList = listOf(
                    TimerItem("1", "4 min, 30 sec", 270, 264, true),
                    TimerItem("2", "10 min", 600, 576, true)
                ),
                activeTimerList = listOf(
                    TimerItem("r1", "4 min, 30 sec", 270, 270, false),
                    TimerItem("r2", "10 min", 600, 600, false)
                )
            )
        }
    }

    // Hàm duy nhất nhận Event từ giao diện gửi lên
    fun onEvent(event: TimerUiEvent) {
        when (event) {
            TimerUiEvent.ClickAddButton -> {
                _uiState.update { it.copy(currentScreen = TimerNavigationState.SETUP) }
            }
            TimerUiEvent.ClickBackButton -> {
                _uiState.update { it.copy(currentScreen = TimerNavigationState.LIST) }
            }
            is TimerUiEvent.StartNewTimer -> {
                createNewTimer(event.hours, event.minutes, event.seconds)
            }
            is TimerUiEvent.ToggleTimer -> {
                toggleTimerStatus(event.timerId)
            }
            is TimerUiEvent.CancelTimer -> {
                cancelTimerItem(event.timerId)
            }
        }
    }

    private fun createNewTimer(h: Int, m: Int, s: Int) {
        val total = (h * 3600 + m * 60 + s).toLong()
        val label = if (h > 0) "$h hr" else if (m > 0) "$m min" else "$s sec"
        val newTimer = TimerItem(
            id = System.currentTimeMillis().toString(),
            label = label,
            totalSeconds = total,
            remainingSeconds = total,
            isRunning = true
        )

        val cal = Calendar.getInstance()
        cal.add(Calendar.SECOND, total.toInt())
        val targetStr = String.format(Locale.getDefault(), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        _uiState.update {
            it.copy(
                timerList = listOf(newTimer) + it.timerList,
                selectedTimer = newTimer,
                targetTimeString = targetStr,
                currentScreen = TimerNavigationState.ACTIVE
            )
        }

        // Schedule WorkManager để gửi notification khi hết giờ
        scheduleTimerWork(newTimer.id, label, total)
        observeWorkStatus(newTimer.id)
    }

    private fun toggleTimerStatus(id: String) {
        _uiState.update { state ->
            val updatedList = state.timerList.map { i ->
                if (i.id == id) i.copy(isRunning = !i.isRunning) else i
            }
            val updatedSelected = state.selectedTimer?.let { active ->
                if (active.id == id) active.copy(isRunning = !active.isRunning) else active
            }

            // Xử lý WorkManager khi toggle
            val timerItem = state.timerList.find { it.id == id }
            timerItem?.let { item ->
                if (item.isRunning) {
                    cancelTimerWork(id)
                } else {
                    val remaining = item.remainingSeconds
                    if (remaining > 0) {
                        scheduleTimerWork(id, item.label, remaining)
                        observeWorkStatus(id)
                    }
                }
            }

            state.copy(timerList = updatedList, selectedTimer = updatedSelected)
        }
    }

    private fun cancelTimerItem(id: String) {
        cancelTimerWork(id)

        _uiState.update { state ->
            state.copy(
                timerList = state.timerList.filter { it.id != id },
                selectedTimer = null,
                currentScreen = TimerNavigationState.LIST
            )
        }
    }

    private fun observeWorkStatus(timerId: String) {
        viewModelScope.launch {
            workManager.getWorkInfosByTagFlow(TimerCountdownWorker.WORK_NAME_PREFIX + timerId)
                .collect { workInfos ->
                    val workInfo = workInfos.firstOrNull() ?: return@collect
                    
                    _uiState.update { state ->
                        var updatedList = state.timerList
                        var updatedSelected = state.selectedTimer

                        if (workInfo.state == WorkInfo.State.RUNNING) {
                            val remaining = workInfo.progress.getLong(TimerCountdownWorker.KEY_REMAINING_SECONDS, -1L)
                            if (remaining >= 0) {
                                updatedList = updatedList.map {
                                    if (it.id == timerId) it.copy(isRunning = true, remainingSeconds = remaining) else it
                                }
                                if (updatedSelected?.id == timerId) {
                                    updatedSelected = updatedSelected.copy(isRunning = true, remainingSeconds = remaining)
                                }
                            }
                        } else if (workInfo.state == WorkInfo.State.SUCCEEDED || workInfo.state == WorkInfo.State.FAILED) {
                            updatedList = updatedList.map {
                                if (it.id == timerId) it.copy(isRunning = false, remainingSeconds = 0) else it
                            }
                            if (updatedSelected?.id == timerId) {
                                updatedSelected = updatedSelected.copy(isRunning = false, remainingSeconds = 0)
                            }
                        }

                        state.copy(timerList = updatedList, selectedTimer = updatedSelected)
                    }
                }
        }
    }

    // =============================================
    // WORKMANAGER INTEGRATION
    // =============================================

    private fun scheduleTimerWork(timerId: String, label: String, delaySeconds: Long) {
        val workData = workDataOf(
            TimerCountdownWorker.KEY_TIMER_ID to timerId,
            TimerCountdownWorker.KEY_TIMER_LABEL to label,
            TimerCountdownWorker.KEY_TOTAL_SECONDS to delaySeconds
        )

        val workRequest = OneTimeWorkRequestBuilder<TimerCountdownWorker>()
            .setInputData(workData)
            .addTag(TimerCountdownWorker.WORK_NAME_PREFIX + timerId)
            .build()

        workManager.enqueue(workRequest)
    }

    private fun cancelTimerWork(timerId: String) {
        workManager.cancelAllWorkByTag(TimerCountdownWorker.WORK_NAME_PREFIX + timerId)
    }
}