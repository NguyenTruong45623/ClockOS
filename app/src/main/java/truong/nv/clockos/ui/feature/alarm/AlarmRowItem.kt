package truong.nv.clockos.ui.feature.alarm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import truong.nv.clockos.data.models.AlarmEntity
import truong.nv.clockos.ui.theme.IosColor
import kotlin.math.roundToInt

@Composable
fun AlarmRowItem(
    alarm: AlarmEntity,
    isEditMode: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    truong.nv.clockos.ui.components.FluidSwipeToDeleteWrapper(
        isSwipeEnabled = !isEditMode,
        onDelete = onDelete,
        onClick = onClick,
        modifier = Modifier.height(90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
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
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun AlarmRowItemPreview() {
    AlarmRowItem(
        alarm = AlarmEntity(
            id = 1,
            hour = 7,
            minute = 30,
            label = "Đi làm",
            isEnabled = true,
            repeatDays = listOf(1, 2, 3, 4, 5),
            isSnoozeEnabled = true,
            snoozeDuration = 9,
            soundName = "Radar"
        ),
        isEditMode = false,
        onToggle = {},
        onClick = {},
        onDelete = {}
    )
}