package truong.nv.clockos.ui.feature.time

import truong.nv.clockos.data.models.TimerItem

data class TimerUiState(
    val currentScreen: TimerNavigationState = TimerNavigationState.LIST,
    val timerList: List<TimerItem> = emptyList(),
    val recentList: List<TimerItem> = emptyList(),
    val selectedTimer: TimerItem? = null,
    val targetTimeString: String = "00:00"
)

sealed class TimerUiEvent {
    data object ClickAddButton : TimerUiEvent()
    data object ClickBackButton : TimerUiEvent()
    data class StartNewTimer(val hours: Int, val minutes: Int, val seconds: Int) : TimerUiEvent()
    data class ToggleTimer(val timerId: String) : TimerUiEvent()
    data class CancelTimer(val timerId: String) : TimerUiEvent()
}