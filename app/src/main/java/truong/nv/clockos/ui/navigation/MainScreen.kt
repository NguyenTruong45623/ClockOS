package truong.nv.clockos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.ui.feature.alarm.AlarmScreen
import truong.nv.clockos.ui.feature.stopTime.StopTimeScreen
import truong.nv.clockos.ui.feature.time.TimerScreen
import truong.nv.clockos.ui.feature.worldClock.WorldClockScreen

enum class MainTab(val icon: ImageVector, val title: String) {
    WorldClock(Icons.Default.Public, "World Clock"),
    Alarm(Icons.Default.Alarm, "Alarm"),
    StopTime(Icons.Default.Timer, "Stop Time"),
    Time(Icons.Default.AccessTime, "Time")
}

@Composable
fun MainScreen(
    navigator: AppNavigator,
) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Alarm) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
            ) {
                // 1. ĐƯỜNG KẺ BIÊN TRÊN SIÊU MẢNH KỂU IOS
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                )

                // 2. THANH BOTTOM BAR CHỨA CÁC TAB
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainTab.entries.forEach { tab ->
                        MainTabItem(
                            tab = tab,
                            isSelected = currentTab == tab,
                            onClick = { currentTab = tab }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                MainTab.WorldClock -> WorldClockScreen(navigator)
                MainTab.Alarm -> AlarmScreen(navigator)
                MainTab.StopTime -> StopTimeScreen(navigator)
                MainTab.Time -> TimerScreen(navigator)
            }
        }
    }
}

@Composable
fun RowScope.MainTabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Định nghĩa màu sắc nội dung thích ứng theo trạng thái active
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Column(
        modifier = modifier
            .weight(1f)
            .height(52.dp) // Cố định chiều cao khối bầu dục
            .clip(RoundedCornerShape(26.dp)) // Bo tròn tối đa góc tạo hình bầu dục viên thuốc
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Tắt gợn sóng mặc định để ko đè lên hình bầu dục
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        // Icon
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.title,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Nhãn chữ
        Text(
            text = tab.title,
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = (-0.1).sp
        )
    }
}