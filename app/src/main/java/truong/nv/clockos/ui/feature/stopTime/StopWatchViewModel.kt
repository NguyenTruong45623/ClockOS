package truong.nv.clockos.ui.feature.stopTime

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import truong.nv.clockos.data.StopWatchPref
import truong.nv.clockos.data.dao.StopWatchDao
import truong.nv.clockos.data.models.StopWatchEntity
import javax.inject.Inject

@HiltViewModel
class StopWatchViewModel @Inject constructor(
    private val repository: StopWatchDao
) : ViewModel() {

    private val _state = MutableStateFlow(
        StopWatchState(
            elapsedTime = StopWatchPref.elapsedTime,
            isRunning = StopWatchPref.isRunning
        )
    )

    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    init {

        if (StopWatchPref.isRunning) {
            onAppForeground()
        }
    }

    fun onAction(event: StopWatchEvent) {
        when (event) {
            StopWatchEvent.Start -> onStart()
            StopWatchEvent.Stop -> onStop()
            StopWatchEvent.Reset -> onReset()
            StopWatchEvent.Lap -> onLap()
            StopWatchEvent.OnAppBackground -> onAppBackground()
            StopWatchEvent.OnAppForeground -> onAppForeground()
        }
    }

    private fun startTicker() {

        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {

            var lastTick = System.currentTimeMillis()

            while (isActive) {

                delay(10)

                val now = System.currentTimeMillis()

                val delta = now - lastTick

                lastTick = now

                val newElapsed =
                    _state.value.elapsedTime + delta

                _state.update {
                    it.copy(
                        elapsedTime = newElapsed
                    )
                }
                    Log.d("StopWatchViewModel", "startTicker:1111")
            }
        }
    }

    private fun onStart() {

        if (_state.value.isRunning) return

        StopWatchPref.isRunning = true

        StopWatchPref.pauseTimestamp =
            System.currentTimeMillis()

        _state.update {
            it.copy(
                isRunning = true
            )
        }

        startTicker()
    }

    private fun onStop() {

        timerJob?.cancel()
        timerJob = null

        StopWatchPref.isRunning = false

        StopWatchPref.elapsedTime =
            _state.value.elapsedTime

        StopWatchPref.pauseTimestamp = 0L

        _state.update {
            it.copy(
                isRunning = false
            )
        }
    }

    private fun onReset() {

        timerJob?.cancel()
        timerJob = null

        StopWatchPref.isRunning = false
        StopWatchPref.elapsedTime = 0L
        StopWatchPref.pauseTimestamp = 0L

        _state.value = StopWatchState()
    }

    private fun onLap() {
        if (!_state.value.isRunning) return

        val currentElapsed = _state.value.elapsedTime
        // Tính tổng thời gian của các vòng trước đó
        val previousLapsTotal = _state.value.laps.sumOf { it.elapsedTime }

        // Thời gian thực tế của riêng vòng này
        val currentLapTime = currentElapsed - previousLapsTotal

        val newLap = StopWatchEntity(
            // Truyền các tham số cần thiết của entity, ví dụ:
            elapsedTime = currentLapTime
        )

        _state.update {
            it.copy(
                // Đẩy lap mới nhất lên đầu danh sách
                laps = listOf(newLap) + it.laps
            )
        }
    }

    private fun onAppBackground() {

        if (!_state.value.isRunning) return

        StopWatchPref.elapsedTime =
            _state.value.elapsedTime

        StopWatchPref.pauseTimestamp =
            System.currentTimeMillis()

        timerJob?.cancel()
        timerJob = null
    }

    private fun onAppForeground() {

        if (!StopWatchPref.isRunning) return

        val additionalTime =
            System.currentTimeMillis() -
                    StopWatchPref.pauseTimestamp

        val newElapsed =
            StopWatchPref.elapsedTime +
                    additionalTime

        StopWatchPref.elapsedTime =
            newElapsed

        _state.update {
            it.copy(
                elapsedTime = newElapsed,
                isRunning = true
            )
        }

        startTicker()
    }

    override fun onCleared() {

        timerJob?.cancel()
        timerJob = null

        super.onCleared()
    }
}

data class StopWatchState(
    val elapsedTime: Long = 0L,
    val laps: List<StopWatchEntity> = emptyList(),
    val isRunning: Boolean = false,
)

sealed class StopWatchEvent{
    object Start : StopWatchEvent()
    object Stop : StopWatchEvent()
    object Reset : StopWatchEvent()
    object Lap : StopWatchEvent()
    object OnAppBackground : StopWatchEvent()
    object OnAppForeground : StopWatchEvent()
}