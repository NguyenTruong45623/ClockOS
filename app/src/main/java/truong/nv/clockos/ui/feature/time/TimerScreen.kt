package truong.nv.clockos.ui.feature.time

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import truong.nv.clockos.data.models.TimerItem
import truong.nv.clockos.helper.formatSecondsToTimeString
import truong.nv.clockos.ui.components.ControlButton
import truong.nv.clockos.ui.components.ProgressRing
import truong.nv.clockos.ui.components.SwipeableTimerRowItem
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor

@Composable
fun TimerScreen(
    navigator: AppNavigator,
    viewModel: TimerViewModel = viewModel()
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
                recentTimers = state.recentList,
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
// MÀN HÌNH 1: DANH SÁCH BỘ HẸN GIỜ (LIST)
// ==========================================
@Composable
fun TimerListScreen(
    activeTimers: List<TimerItem>,
    recentTimers: List<TimerItem>,
    onAddClick: () -> Unit,
    onItemClick: (TimerItem) -> Unit,
    onToggleItem: (TimerItem) -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sửa", color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)
                Text(text = "Hẹn giờ", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            items(activeTimers, key = { it.id }) { timer ->
                SwipeableTimerRowItem(
                    timer = timer,
                    showProgressRing = true,
                    onClick = { onItemClick(timer) },
                    onToggle = { onToggleItem(timer) },
                    onDelete = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Gần đây", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(recentTimers, key = { it.id }) { timer ->
                SwipeableTimerRowItem(
                    timer = timer,
                    showProgressRing = false,
                    onClick = {},
                    onToggle = { onToggleItem(timer) },
                    onDelete = {}
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
            }
        }
    }
}

// ==========================================
// MÀN HÌNH 2: CẤU HÌNH / THIẾT LẬP (SETUP)
// ==========================================
@Composable
fun TimerSetupScreen(
    onBackClick: () -> Unit,
    onStartClick: (hours: Int, minutes: Int, seconds: Int) -> Unit,
    onToneRowClick: () -> Unit,
    selectedToneName: String = "Hướng tâm (Mặc định)"
) {
    var selectedHour by remember { mutableStateOf(0) }
    var selectedMinute by remember { mutableStateOf(0) }
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
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Text(
                text = "Hủy",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp,
                modifier = Modifier.clickable { onBackClick() }
            )
        }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                text = "Hủy",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                enabled = false,
                onClick = {}
            )

            ControlButton(
                text = "Bắt đầu",
                backgroundColor = if (isTimeSelected) IosColor.Green.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                textColor = if (isTimeSelected) IosColor.GreenLight else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                enabled = isTimeSelected,
                onClick = { onStartClick(selectedHour, selectedMinute, selectedSecond) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TimerSettingRow(title = "Nhãn", value = "Hẹn giờ", onClick = {})
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
            TimerSettingRow(title = "Khi hẹn giờ kết thúc", value = selectedToneName, highlightValue = true, onClick = onToneRowClick)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// MÀN HÌNH 3: ĐANG ĐẾM NGƯỢC (ACTIVE SCREEN)
// ==========================================
@Composable
fun TimerActiveScreen(
    remainingSeconds: Long,
    totalSeconds: Long,
    targetTimeString: String,
    isRunning: Boolean,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onToggleClick: () -> Unit
) {
    val progressTarget = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${totalSeconds / 60} phút",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(48.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ProgressRing(progress = animatedProgress)

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "🔔 ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text(text = targetTimeString, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatSecondsToTimeString(remainingSeconds),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                text = "Hủy",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                onClick = onCancelClick
            )

            ControlButton(
                text = if (isRunning) "Tạm dừng" else "Tiếp tục",
                backgroundColor = if (isRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else IosColor.Green.copy(alpha = 0.15f),
                textColor = if (isRunning) MaterialTheme.colorScheme.primary else IosColor.GreenLight,
                onClick = onToggleClick
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// CÁC SUB-COMPONENTS ĐỒ HỌA NHỎ
// ==========================================
@Composable
fun TimerRowItem(
    timer: TimerItem,
    showProgressRing: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = formatSecondsToTimeString(timer.remainingSeconds),
                color = if (showProgressRing && timer.isRunning) MaterialTheme.colorScheme.onBackground else Color.Gray,
                fontSize = 38.sp,
                fontWeight = FontWeight.Light
            )
            Text(text = timer.label, color = Color.Gray, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (showProgressRing) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else IosColor.Green.copy(alpha = 0.12f)
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (showProgressRing) {
                val progress = if (timer.totalSeconds > 0) timer.remainingSeconds.toFloat() / timer.totalSeconds else 0f
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawCircle(color = IosColor.Orange.copy(alpha = 0.1f), style = Stroke(2.dp.toPx()))
                    drawArc(
                        color = IosColor.Orange,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Icon(
                    imageVector = if (timer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = IosColor.GreenLight, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun TimerSettingRow(title: String, value: String, highlightValue: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = if (highlightValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}