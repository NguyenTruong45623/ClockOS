package truong.nv.clockos.ui.feature.time

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.ui.components.ControlButton
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.theme.IosColor

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
//                WheelPicker(items = (0..23).toList(), label = "giờ", onItemSelected = { selectedHour = it })
//                Spacer(modifier = Modifier.width(8.dp))
//                WheelPicker(items = (0..59).toList(), label = "phút", onItemSelected = { selectedMinute = it })
//                Spacer(modifier = Modifier.width(8.dp))
//                WheelPicker(items = (0..59).toList(), label = "giây", onItemSelected = { selectedSecond = it })
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