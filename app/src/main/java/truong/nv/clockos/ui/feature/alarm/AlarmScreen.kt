package truong.nv.clockos.ui.feature.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import truong.nv.clockos.data.models.AlarmEntity
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    navigator: AppNavigator,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAlarmSheet by remember { mutableStateOf(false) }
    var selectedAlarmForEdit by remember { mutableStateOf<AlarmEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edit Mode Button (Sửa / Xong)
                    Text(
                        text = if (uiState.isEditMode) "Xong" else "Sửa",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { viewModel.toggleEditMode() }
                    )

                    // Add Button (+)
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm báo thức",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                selectedAlarmForEdit = null
                                showAlarmSheet = true
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Page Large Title "Báo thức"
                Text(
                    text = "Báo thức",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (uiState.alarms.isEmpty()) {
                // Empty state view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Không có báo thức",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nhấp vào dấu + ở góc trên cùng bên phải để tạo báo thức mới.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = uiState.alarms,
                        key = { it.id }
                    ) { alarm ->
                        AlarmRowItem(
                            alarm = alarm,
                            isEditMode = uiState.isEditMode,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onClick = {
                                if (uiState.isEditMode) {
                                    selectedAlarmForEdit = alarm
                                    showAlarmSheet = true
                                }
                            },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Alarm Custom Full Screen Dialog
    if (showAlarmSheet) {
        AlarmEditDialog(
            alarm = selectedAlarmForEdit,
            onDismiss = { showAlarmSheet = false },
            onSave = { hour, minute, repeatDays, label, isSnoozeEnabled, snoozeDuration, soundName ->
                viewModel.saveAlarm(
                    id = selectedAlarmForEdit?.id ?: 0,
                    hour = hour,
                    minute = minute,
                    repeatDays = repeatDays,
                    label = label,
                    isSnoozeEnabled = isSnoozeEnabled,
                    snoozeDuration = snoozeDuration,
                    soundName = soundName
                )
                showAlarmSheet = false
            },
            onDelete = {
                selectedAlarmForEdit?.let { viewModel.deleteAlarm(it) }
                showAlarmSheet = false
            }
        )
    }
}

@Composable
fun AlarmRowItem(
    alarm: AlarmEntity,
    isEditMode: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Minus Delete Button in Edit Mode
        AnimatedVisibility(
            visible = isEditMode,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "Xóa",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Time and Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                val amPmStr = if (alarm.hour < 12) "AM" else "PM"
                val displayHour = when {
                    alarm.hour == 0 -> 12
                    alarm.hour > 12 -> alarm.hour - 12
                    else -> alarm.hour
                }
                
                Text(
                    text = String.format("%02d:%02d", displayHour, alarm.minute),
                    color = if (alarm.isEnabled && !isEditMode) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = amPmStr,
                    color = if (alarm.isEnabled && !isEditMode) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Subtitle: Label and Repeat Schedule
            val repeatStr = getRepeatSummary(alarm.repeatDays)
            val subtitleText = if (alarm.label.isNotBlank() && alarm.label != "Báo thức") {
                "${alarm.label}, $repeatStr"
            } else {
                repeatStr
            }
            
            Text(
                text = subtitleText,
                color = if (alarm.isEnabled && !isEditMode) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }

        // Switch or Chevron Right Icon
        if (isEditMode) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit Detail",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = IosColor.Green,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun AlarmEditDialog(
    alarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, repeatDays: List<Int>, label: String, isSnoozeEnabled: Boolean, snoozeDuration: Int, soundName: String) -> Unit,
    onDelete: () -> Unit
) {
    var hour by remember { mutableStateOf(alarm?.hour ?: 7) }
    var minute by remember { mutableStateOf(alarm?.minute ?: 0) }
    var label by remember { mutableStateOf(alarm?.label ?: "Báo thức") }
    val repeatDays = remember { mutableStateListOf<Int>().apply { alarm?.let { addAll(it.repeatDays) } } }
    var isSnoozeEnabled by remember { mutableStateOf(alarm?.isSnoozeEnabled ?: true) }
    var snoozeDuration by remember { mutableStateOf(alarm?.snoozeDuration ?: 9) }
    var soundName by remember { mutableStateOf(alarm?.soundName ?: "Mặc định") }
    
    var showSnoozeDurationSheet by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen style
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black // iOS Style edit screen is always pitch black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hủy",
                        color = IosColor.Orange,
                        fontSize = 17.sp,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                    Text(
                        text = if (alarm == null) "Thêm báo thức" else "Sửa báo thức",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lưu",
                        color = IosColor.Orange,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onSave(hour, minute, repeatDays.toList(), label, isSnoozeEnabled, snoozeDuration, soundName)
                        }
                    )
                }

                // Time Pickers (Wheel)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WheelPicker(
                            items = (0..23).toList(),
                            label = "giờ",
                            onItemSelected = { hour = it }
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        WheelPicker(
                            items = (0..59).toList(),
                            label = "phút",
                            onItemSelected = { minute = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options List (Grouped iOS-style card container)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IosColor.DarkBackgroundSecondary)
                ) {
                    // Repeat selector (iOS Style Row triggering BottomSheet)
                    var showRepeatSheet by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRepeatSheet = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lặp lại",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = getRepeatSummary(repeatDays),
                                color = Color.Gray,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Chọn ngày lặp lại",
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (showRepeatSheet) {
                        RepeatBottomSheet(
                            selectedDays = repeatDays.toList(),
                            onDayToggled = { day ->
                                if (repeatDays.contains(day)) {
                                    repeatDays.remove(day)
                                } else {
                                    repeatDays.add(day)
                                }
                            },
                            onDismiss = { showRepeatSheet = false }
                        )
                    }

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.15f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    // Label input row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nhãn",
                            color = Color.White,
                            fontSize = 17.sp,
                            modifier = Modifier.width(80.dp)
                        )
                        TextField(
                            value = label,
                            onValueChange = { label = it },
                            placeholder = { Text("Báo thức", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.15f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    // Sound indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Âm thanh",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = soundName,
                            color = Color.Gray,
                            fontSize = 17.sp
                        )
                    }

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.15f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    // Snooze option switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Báo lại (Snooze)",
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Switch(
                            checked = isSnoozeEnabled,
                            onCheckedChange = { isSnoozeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IosColor.Orange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    if (isSnoozeEnabled) {
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.15f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSnoozeDurationSheet = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Thời gian báo lại",
                                color = Color.White,
                                fontSize = 17.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$snoozeDuration Phút",
                                    color = Color.Gray,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Chọn thời gian",
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (showSnoozeDurationSheet) {
                    SnoozeDurationBottomSheet(
                        selectedDuration = snoozeDuration,
                        onDurationSelected = { snoozeDuration = it },
                        onDismiss = { showSnoozeDurationSheet = false }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Delete Button (visible only when editing an existing alarm)
                if (alarm != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(IosColor.DarkBackgroundSecondary)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Xóa báo thức",
                            color = Color.Red,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun getRepeatSummary(repeatDays: List<Int>): String {
    if (repeatDays.isEmpty()) return "Chỉ một lần"
    if (repeatDays.size == 7) return "Hàng ngày"
    val isWeekdays = repeatDays.containsAll(listOf(1, 2, 3, 4, 5)) && repeatDays.size == 5
    if (isWeekdays) return "Ngày thường"
    val isWeekends = repeatDays.containsAll(listOf(6, 7)) && repeatDays.size == 2
    if (isWeekends) return "Cuối tuần"

    val dayNames = mapOf(
        1 to "T2",
        2 to "T3",
        3 to "T4",
        4 to "T5",
        5 to "T6",
        6 to "T7",
        7 to "CN"
    )
    return repeatDays.sorted().map { dayNames[it] ?: "" }.joinToString(", ")
}