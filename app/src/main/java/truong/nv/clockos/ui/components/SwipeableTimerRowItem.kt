package truong.nv.clockos.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.data.models.TimerItem
import truong.nv.clockos.helper.formatSecondsToTimeString
import truong.nv.clockos.ui.theme.IosColor
import kotlin.math.roundToInt

enum class DragAnchors {
    Start,
    End
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableTimerRowItem(
    timer: TimerItem,
    showProgressRing: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { 80.dp.toPx() }

    // Animation trượt nảy (bắt buộc trong Compose mới)
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    // Cấu hình trạng thái vuốt chuẩn Compose 1.6+
    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(durationMillis = 250),
            decayAnimationSpec = decayAnimationSpec // Truyền thêm param này
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    DragAnchors.Start at 0f
                    DragAnchors.End at -actionWidthPx
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TẦNG NỀN: Nút Thùng rác
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(80.dp)
                .fillMaxHeight()
                .background(Color.Red)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // TẦNG TRÊN: Nội dung chính
        Row(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    // Fix lỗi crash NaN khi state chưa kịp khởi tạo offset
                    val currentOffset = if (draggableState.offset.isNaN()) 0f else draggableState.offset
                    IntOffset(currentOffset.roundToInt(), 0)
                }
                .anchoredDraggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal
                )
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
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
}