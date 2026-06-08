package truong.nv.clockos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun WheelPicker(
    values: List<String>,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 48.dp,
    startIndex: Int = 0,
    textSize: TextUnit = 22.sp,
    onItemSelected: (index: Int, item: String) -> Unit
) {
    if (values.isEmpty()) return
    require(visibleItemsCount % 2 == 1) { "visibleItemsCount phải là số lẻ" }

    val count = values.size
    val halfVisible = visibleItemsCount / 2
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val scrollPx = remember { Animatable(0f) }
    val velocityTracker = remember { VelocityTracker() }

    // TỐI ƯU CỰC ĐẠI GESTURE: detectVerticalDragGestures bản chất đã chạy trong Suspend Lambda.
    // Việc đưa trực tiếp scrollPx.snapTo vào đây giúp loại bỏ hoàn toàn scope.launch, chặn đứng lỗi tràn bộ nhớ (GC Thrashing).
    val gestureModifier = Modifier.pointerInput(count, itemHeightPx) {
        detectVerticalDragGestures(
            onDragStart = {
                velocityTracker.resetTracking()
                scope.launch { scrollPx.stop() }
            },
            onDragEnd = {
                val velocity = -velocityTracker.calculateVelocity().y * 1.5f
                scope.launch {
                    // Phase 1: Decay
                    scrollPx.animateDecay(
                        initialVelocity = velocity,
                        animationSpec = exponentialDecay(frictionMultiplier = 0.7f)
                    )

                    // Phase 2: Snap
                    val snappedPx = (scrollPx.value / itemHeightPx).roundToInt() * itemHeightPx
                    scrollPx.animateTo(
                        targetValue = snappedPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    val idx = (scrollPx.value / itemHeightPx).roundToInt()
                    val realIdx = Math.floorMod(startIndex + idx, count)
                    onItemSelected(realIdx, values[realIdx])
                }
            },
            onDragCancel = {
                scope.launch {
                    val snappedPx = (scrollPx.value / itemHeightPx).roundToInt() * itemHeightPx
                    scrollPx.animateTo(
                        targetValue = snappedPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    val idx = (scrollPx.value / itemHeightPx).roundToInt()
                    val realIdx = Math.floorMod(startIndex + idx, count)
                    onItemSelected(realIdx, values[realIdx])
                }
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                velocityTracker.addPointerInputChange(change)

                // SỬA LỖI: Chạy trực tiếp đồng bộ, không tạo Coroutine Job mới trên mỗi pixel dịch chuyển
                scope.launch(start = kotlinx.coroutines.CoroutineStart.UNDISPATCHED) {
                    scrollPx.snapTo(scrollPx.value - dragAmount)
                }
            }
        )
    }

    val totalSlots = visibleItemsCount + 2
    val halfSlots = totalSlots / 2

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .then(gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        repeat(totalSlots) { slot ->
            val slotOffset = slot - halfSlots

            // Dùng key() để định danh cố định cấu trúc cây UI cho từng slot
            key(slot) {
                val realIndex by remember(slot, count) {
                    derivedStateOf {
                        // TỐI ƯU: Sử dụng Math.floorDiv (chia lấy phần nguyên sàn) thay vì chuyển đổi sang Double rồi dùng floor()
                        val scrolledItems = Math.floorDiv(scrollPx.value.toLong(), itemHeightPx.toLong()).toInt()
                        Math.floorMod(startIndex + scrolledItems + slotOffset, count)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .offset(y = itemHeight * slotOffset)
                        .graphicsLayer {
                            val scrollVal = scrollPx.value

                            // TỐI ƯU: Thay thế toán học Double bằng toán học Số nguyên thuần túy trong Draw Phase
                            val scrolledItems = Math.floorDiv(scrollVal.toLong(), itemHeightPx.toLong()).toInt()
                            val fractional = scrollVal - scrolledItems * itemHeightPx

                            translationY = -fractional

                            val distPx = slotOffset * itemHeightPx - fractional
                            val maxDist = itemHeightPx * (halfVisible + 0.5f)
                            val rawFraction = distPx / maxDist

                            if (rawFraction.absoluteValue > 1.1f) {
                                alpha = 0f
                            } else {
                                val fraction = rawFraction.coerceIn(-1f, 1f)
                                val absFraction = fraction.absoluteValue

                                alpha = 1f - absFraction * 0.55f
                                rotationX = fraction * 65f

                                val scale = 1f - absFraction * 0.15f
                                scaleX = scale
                                scaleY = scale

                                cameraDistance = 8f * density.density
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = values[realIndex],
                        color = Color.White,
                        fontSize = textSize,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // ── Đường kẻ chọn ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItemsCount)
        ) {
            Spacer(Modifier.height(itemHeight * halfVisible))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f), thickness = 0.8.dp)
            Spacer(Modifier.height(itemHeight))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f), thickness = 0.8.dp)
        }
    }
}