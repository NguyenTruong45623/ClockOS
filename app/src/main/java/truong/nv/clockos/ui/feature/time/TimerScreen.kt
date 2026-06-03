package truong.nv.clockos.ui.feature.time

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import truong.nv.clockos.data.models.TimerItem
import truong.nv.clockos.ui.navigation.AppNavigator

// ==========================================
// ĐỊNH NGHĨA ANNOTATION PREVIEW CHUNG
// ==========================================
@Preview(name = "Light Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews

// ==========================================
// MÀN HÌNH CHÍNH
// ==========================================
@Composable
fun TimerScreen(
    navigator: AppNavigator,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.currentScreen != TimerNavigationState.LIST) {
        BackHandler {
            viewModel.onEvent(TimerUiEvent.ClickBackButton)
        }
    }

    when (state.currentScreen) {
        TimerNavigationState.LIST -> {
            TimerListScreen(
                activeTimers = state.timerList,
                recentTimers = state.activeTimerList,
                onAddClick = { viewModel.onEvent(TimerUiEvent.ClickAddButton) },
                onItemClick = { item ->
                    viewModel.onEvent(TimerUiEvent.ToggleTimer(item.id))
                    state.copy(selectedTimer = item)
                    viewModel.onEvent(TimerUiEvent.StartNewTimer(0, 0, item.remainingSeconds.toInt()))
                },
                onToggleItem = { item -> viewModel.onEvent(TimerUiEvent.ToggleTimer(item.id)) }
            )
        }
        TimerNavigationState.SETUP -> {
            TimerSetupScreen(
                onBackClick = { viewModel.onEvent(TimerUiEvent.ClickBackButton) },
                onStartClick = { h, m, s -> viewModel.onEvent(TimerUiEvent.StartNewTimer(h, m, s)) },
                onToneRowClick = {}
            )
        }
        TimerNavigationState.ACTIVE -> {
            state.selectedTimer?.let { item ->
                TimerActiveScreen(
                    remainingSeconds = item.remainingSeconds,
                    totalSeconds = item.totalSeconds,
                    targetTimeString = state.targetTimeString,
                    isRunning = item.isRunning,
                    onBackClick = { viewModel.onEvent(TimerUiEvent.ClickBackButton) },
                    onCancelClick = { viewModel.onEvent(TimerUiEvent.CancelTimer(item.id)) },
                    onToggleClick = { viewModel.onEvent(TimerUiEvent.ToggleTimer(item.id)) }
                )
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@ThemePreviews
@Composable
fun PreviewTimerListScreen() {
    MaterialTheme {
        TimerListScreen(
            activeTimers = listOf(
                TimerItem(id = "1", label = "Luộc trứng", totalSeconds = 600, remainingSeconds = 450, isRunning = true),
                TimerItem(id = "2", label = "Đọc sách", totalSeconds = 1800, remainingSeconds = 1800, isRunning = false)
            ),
            recentTimers = listOf(
                TimerItem(id = "3", label = "Tập thể dục", totalSeconds = 3600, remainingSeconds = 0, isRunning = false),
                TimerItem(id = "4", label = "Nấu cơm", totalSeconds = 2700, remainingSeconds = 0, isRunning = false)
            ),
            onAddClick = {},
            onItemClick = {},
            onToggleItem = {}
        )
    }
}

@ThemePreviews
@Composable
fun PreviewTimerSetupScreen() {
    MaterialTheme {
        TimerSetupScreen(
            onBackClick = {},
            onStartClick = { _, _, _ -> },
            onToneRowClick = {},
            selectedToneName = "Hướng tâm (Mặc định)"
        )
    }
}

@ThemePreviews
@Composable
fun PreviewTimerActiveScreen() {
    MaterialTheme {
        TimerActiveScreen(
            remainingSeconds = 250,
            totalSeconds = 600,
            targetTimeString = "10:30",
            isRunning = true,
            onBackClick = {},
            onCancelClick = {},
            onToggleClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun PreviewTimerSettingRow() {
    MaterialTheme {
        androidx.compose.foundation.layout.Column {
            TimerSettingRow(
                title = "Nhãn",
                value = "Hẹn giờ",
                onClick = {}
            )
            TimerSettingRow(
                title = "Khi hẹn giờ kết thúc",
                value = "Hướng tâm",
                highlightValue = true,
                onClick = {}
            )
        }
    }
}