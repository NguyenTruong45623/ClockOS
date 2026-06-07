package truong.nv.clockos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FluidSwipeToDeleteWrapper(
    modifier: Modifier = Modifier,
    isSwipeEnabled: Boolean = true,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val buttonWidth = 80.dp
    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val buttonWidthPx = with(density) { buttonWidth.toPx() }
    val deleteThresholdPx = with(density) { (screenWidthDp / 2).toPx() }

    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var isDeleted by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    // Reset offset if swipe is disabled
    LaunchedEffect(isSwipeEnabled) {
        if (!isSwipeEnabled && offsetX.value < 0f) {
            offsetX.animateTo(0f)
        }
    }

    if (isDeleted) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background layer: match parent size to get height, then align the red background inside
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(with(density) { (-offsetX.value).toDp() }.coerceAtLeast(buttonWidth))
                    .fillMaxHeight()
                    .clickable {
                        if (isSwipeEnabled) {
                            coroutineScope.launch {
                                offsetX.animateTo(-3000f, animationSpec = androidx.compose.animation.core.tween(300))
                                isDeleted = true
                                onDelete()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val targetWidth = if (isDragging) {
                    val dragWidth = with(density) { (-offsetX.value).toDp() }
                    (dragWidth - 32.dp).coerceAtLeast(48.dp)
                } else {
                    48.dp
                }
                val animatedWidth by androidx.compose.animation.core.animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = if (isDragging) androidx.compose.animation.core.snap() else androidx.compose.animation.core.tween(300),
                    label = "delete_btn_width"
                )

                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(48.dp)
                        .background(Color.Red, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Foreground Content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(isSwipeEnabled) {
                    if (!isSwipeEnabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                offsetX.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            coroutineScope.launch {
                                if (offsetX.value < -deleteThresholdPx) {
                                    offsetX.animateTo(-3000f, animationSpec = androidx.compose.animation.core.tween(300))
                                    isDeleted = true
                                    onDelete()
                                } else if (offsetX.value < -buttonWidthPx / 2) {
                                    offsetX.animateTo(-buttonWidthPx, animationSpec = androidx.compose.animation.core.tween(300))
                                } else {
                                    offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(300))
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            coroutineScope.launch {
                                offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(300))
                            }
                        }
                    )
                }
                .clickable {
                    if (offsetX.value < -10f) {
                        coroutineScope.launch {
                            offsetX.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(300))
                        }
                    } else {
                        onClick()
                    }
                }
        ) {
            content()
        }
    }
}
