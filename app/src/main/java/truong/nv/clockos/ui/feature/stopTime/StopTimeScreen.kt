package truong.nv.clockos.ui.feature.stopTime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.sendCommandToTimerService
import truong.nv.clockos.service.TimerService
import truong.nv.clockos.ui.components.ControlButton
import truong.nv.clockos.ui.components.ProgressRing
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor

/**
 * Màn hình đếm ngược (Stop Time / Countdown Timer).
 * Có 2 trạng thái:
 *  - SETUP: Chọn giờ/phút/giây rồi bấm "Bắt đầu"
 *  - ACTIVE: Hiển thị vòng tròn đếm ngược + nút Tạm dừng/Hủy
 */
@Composable
fun StopTimeScreen(
    navigator: AppNavigator
) {
    val context = LocalContext.current

    // Observe state từ TimerService qua static StateFlow
    val serviceState by TimerService.state.collectAsState()

    // Nếu Service đang chạy hoặc tạm dừng (có thời gian còn lại > 0) → hiển thị ACTIVE
    val isActive = serviceState.isRunning || serviceState.timeLeftInMillis > 0

    if (isActive || serviceState.isFinished) {
        StopTimeActiveScreen(
            timeLeftInMillis = serviceState.timeLeftInMillis,
            totalTimeInMillis = serviceState.totalTimeInMillis,
            isRunning = serviceState.isRunning,
            isFinished = serviceState.isFinished,
            onPauseClick = {
                context.sendCommandToTimerService(TimerService.ACTION_PAUSE)
            },
            onResumeClick = {
                context.sendCommandToTimerService(TimerService.ACTION_RESUME)
            },
            onCancelClick = {
                context.sendCommandToTimerService(TimerService.ACTION_CANCEL)
            }
        )
    } else {
        StopTimeSetupScreen(
            onStartClick = { hours, minutes, seconds ->
                val totalMs = ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
                if (totalMs > 0) {
                    context.sendCommandToTimerService(TimerService.ACTION_START, totalMs)
                }
            }
        )
    }
}

// ==========================================
// MÀN HÌNH CHỌN THỜI GIAN (SETUP)
// ==========================================
@Composable
private fun StopTimeSetupScreen(
    onStartClick: (hours: Int, minutes: Int, seconds: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(5) }
    var selectedSecond by remember { mutableStateOf(0) }

    val isTimeSelected = selectedHour > 0 || selectedMinute > 0 || selectedSecond > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Tiêu đề
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Đếm ngược",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Bánh xe chọn thời gian (WheelPicker)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(items = (0..23).toList(), label = "giờ", onItemSelected = { selectedHour = it })
                Spacer(modifier = Modifier.width(8.dp))
                WheelPicker(items = (0..59).toList(), label = "phút", onItemSelected = { selectedMinute = it })
                Spacer(modifier = Modifier.width(8.dp))
                WheelPicker(items = (0..59).toList(), label = "giây", onItemSelected = { selectedSecond = it })
            }
        }

        // Nút điều khiển
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Hủy (disabled ở trạng thái setup)
            ControlButton(
                text = "Hủy",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                enabled = false,
                onClick = {}
            )

            // Nút Bắt đầu
            ControlButton(
                text = "Bắt đầu",
                backgroundColor = if (isTimeSelected) IosColor.Green.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                textColor = if (isTimeSelected) IosColor.GreenLight
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                enabled = isTimeSelected,
                onClick = { onStartClick(selectedHour, selectedMinute, selectedSecond) }
            )
        }

        // Cài đặt âm thanh
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            StopTimeSettingRow(
                title = "Khi hết giờ",
                value = "Mặc định",
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// MÀN HÌNH ĐANG ĐẾM NGƯỢC (ACTIVE)
// ==========================================
@Composable
private fun StopTimeActiveScreen(
    timeLeftInMillis: Long,
    totalTimeInMillis: Long,
    isRunning: Boolean,
    isFinished: Boolean,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    // Tính progress (0f -> 1f)
    val progressTarget = if (totalTimeInMillis > 0) {
        timeLeftInMillis.toFloat() / totalTimeInMillis.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500),
        label = "CountdownProgress"
    )

    // Format thời gian còn lại
    val totalSeconds = timeLeftInMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val timeText = if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Tiêu đề
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFinished) "Hết giờ!" else "Đếm ngược",
                color = if (isFinished) IosColor.Red else MaterialTheme.colorScheme.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Vòng tròn tiến trình + thời gian
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ProgressRing(progress = animatedProgress)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hiển thị trạng thái
                if (isFinished) {
                    Text(
                        text = "⏰",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hết giờ!",
                        color = IosColor.Red,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!isRunning && timeLeftInMillis > 0) {
                    Text(
                        text = "Tạm dừng",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Thời gian đếm ngược
                Text(
                    text = timeText,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
            }
        }

        // Nút điều khiển
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Hủy
            ControlButton(
                text = "Hủy",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                onClick = onCancelClick
            )

            if (isFinished) {
                // Khi hết giờ → Chỉ hiển thị nút "OK" để tắt
                ControlButton(
                    text = "OK",
                    backgroundColor = IosColor.Green.copy(alpha = 0.15f),
                    textColor = IosColor.GreenLight,
                    onClick = onCancelClick
                )
            } else {
                // Nút Tạm dừng / Tiếp tục
                ControlButton(
                    text = if (isRunning) "Tạm dừng" else "Tiếp tục",
                    backgroundColor = if (isRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else IosColor.Green.copy(alpha = 0.15f),
                    textColor = if (isRunning) MaterialTheme.colorScheme.primary
                    else IosColor.GreenLight,
                    onClick = if (isRunning) onPauseClick else onResumeClick
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// SUB-COMPONENT: Setting Row
// ==========================================
@Composable
private fun StopTimeSettingRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 17.sp
        )
    }
}