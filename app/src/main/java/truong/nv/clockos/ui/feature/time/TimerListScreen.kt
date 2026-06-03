package truong.nv.clockos.ui.feature.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.data.models.TimerItem
import truong.nv.clockos.ui.components.SwipeableTimerRowItem

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