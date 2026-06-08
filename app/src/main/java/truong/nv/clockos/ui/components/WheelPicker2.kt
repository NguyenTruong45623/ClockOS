package truong.nv.clockos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * WheelPicker mô phỏng iOS UIPickerView.
 *
 * Kiến trúc RecyclerView:
 * - Pool cố định (visibleItemsCount + 2) slot, không compose/dispose khi cuộn.
 * - scrollPx (Animatable) là scroll position liên tục.
 * - derivedStateOf tính text label → chỉ recompose khi vượt qua ranh giới item.
 * - graphicsLayer đọc scrollPx.value → chỉ invalidate draw layer, không recompose.
 *
 * Fling 2 pha giống iOS:
 * - Phase 1 (Decay): momentum tự nhiên, exponential decay.
 * - Phase 2 (Snap): spring critically-damped, không nảy.
 */
@Composable
fun WheelPicker2(
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

    // ── State: scroll position liên tục (pixel) ─────────────────────────────
    // scrollPx = 0       → startIndex ở giữa
    // scrollPx = +height  → item tiếp theo ở giữa (vuốt lên, cuộn tới)
    // scrollPx = -height  → item trước đó ở giữa (vuốt xuống, cuộn lui)
    val scrollPx = remember { Animatable(0f) }

    // ── Gesture handling ────────────────────────────────────────────────────
    val velocityTracker = remember { VelocityTracker() }

    val gestureModifier = Modifier.pointerInput(count, itemHeightPx) {
        detectVerticalDragGestures(
            onDragStart = {
                velocityTracker.resetTracking()
                // Dừng animation đang chạy (nếu có) khi chạm vào
                scope.launch { scrollPx.stop() }
            },
            onDragEnd = {
                // Đảo dấu: kéo xuống (dragAmount > 0) = cuộn ngược (scrollPx giảm)
                // Nhân 1.5f để tăng tốc độ khởi điểm của fling (nhạy hơn)
                val velocity = -velocityTracker.calculateVelocity().y * 1.5f

                scope.launch {
                    // Phase 1: Decay — momentum tự nhiên kiểu iOS
                    val decayJob = launch {
                        scrollPx.animateDecay(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 0.7f)
                        )
                    }

                    // Liên tục theo dõi vận tốc, khi vòng quay chậm lại (dưới 150px/s),
                    // lập tức dừng decay sớm để "hút" (snap) vào item gần nhất.
                    // (Sớm hơn 1 xíu so với việc chờ dừng hẳn, nhưng không quá gắt)
                    while (decayJob.isActive) {
                        if (scrollPx.velocity.absoluteValue < 150f && scrollPx.velocity.absoluteValue > 0f) {
                            decayJob.cancel()
                            break
                        }
                        kotlinx.coroutines.delay(16)
                    }
                    decayJob.join() // Đảm bảo job đã dừng hoàn toàn

                    // Phase 2: Snap — bắt item gần nhất
                    // Dùng StiffnessMedium để lò xo hút vào chính giữa êm ái hơn
                    val snappedPx =
                        (scrollPx.value / itemHeightPx).roundToInt() * itemHeightPx
                    scrollPx.animateTo(
                        targetValue = snappedPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium 
                        )
                    )

                    // Callback item đã chọn
                    val idx = (scrollPx.value / itemHeightPx).roundToInt()
                    val realIdx = Math.floorMod(startIndex + idx, count)
                    onItemSelected(realIdx, values[realIdx])
                }
            },
            onDragCancel = {
                scope.launch {
                    // Snap về item gần nhất khi gesture bị hủy
                    val snappedPx =
                        (scrollPx.value / itemHeightPx).roundToInt() * itemHeightPx
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
                scope.launch {
                    // Cập nhật scroll position trực tiếp (đảo dấu: kéo xuống = lùi)
                    scrollPx.snapTo(scrollPx.value - dragAmount)
                }
            }
        )
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    // Pool cố định: visibleItemsCount + 2 slot (thêm 1 trên + 1 dưới)
    // để translationY không tạo khoảng trống khi cuộn qua ranh giới item.
    val totalSlots = visibleItemsCount + 2
    val halfSlots = totalSlots / 2

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .then(gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        repeat(totalSlots) { slot ->
            // Vị trí slot so với tâm: -3, -2, -1, 0, 1, 2, 3 (cho 7 slots)
            val slotOffset = slot - halfSlots

            // Text binding: derivedStateOf chỉ trigger recompose khi item THỰC SỰ đổi
            // (khi scroll vượt qua ranh giới item), không phải mỗi frame.
            val realIndex by remember(slot) {
                derivedStateOf {
                    val scrolledItems =
                        floor((scrollPx.value / itemHeightPx).toDouble()).toInt()
                    Math.floorMod(startIndex + scrolledItems + slotOffset, count)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    // Đặt slot vào vị trí cố định so với tâm viewport
                    .offset(y = itemHeight * slotOffset)
                    .graphicsLayer {
                        // Đọc scrollPx.value trong graphicsLayer → chỉ invalidate draw layer,
                        // KHÔNG trigger recomposition.
                        val scrollVal = scrollPx.value
                        val scrolledItems =
                            floor((scrollVal / itemHeightPx).toDouble()).toInt()
                        val fractional = scrollVal - scrolledItems * itemHeightPx

                        // Dịch chuyển mượt — tạo hiệu ứng cuộn liên tục
                        translationY = -fractional

                        // Khoảng cách từ tâm viewport (pixel)
                        val distPx = slotOffset * itemHeightPx - fractional
                        val maxDist = itemHeightPx * (halfVisible + 0.5f)
                        val rawFraction = distPx / maxDist

                        // Slot buffer nằm ngoài vùng hiển thị → ẩn hoàn toàn
                        if (rawFraction.absoluteValue > 1.1f) {
                            alpha = 0f
                        } else {
                            val fraction = rawFraction.coerceIn(-1f, 1f)
                            val absFraction = fraction.absoluteValue

                            // ── iOS UIPickerView Effects ──

                            // Alpha: giữa = 1.0, rìa = ~0.45 (mờ nhưng vẫn đọc được)
                            alpha = 1f - absFraction * 0.55f

                            // Cylinder 3D: xoay quanh trục X tạo hiệu ứng trống xoay
                            // Tăng góc xoay lên 65 độ để thấy rõ độ cong của vòng tròn
                            rotationX = fraction * 65f

                            // Tăng cường 3D: thu nhỏ dần item khi xa tâm
                            val scale = 1f - absFraction * 0.15f
                            scaleX = scale
                            scaleY = scale

                            // Camera distance: giảm xuống 8f để tăng độ méo perspective (thấy rõ 3D hơn)
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

        // ── Đường kẻ chọn (Selection Indicator) giống iOS ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItemsCount)
        ) {
            // Spacer đẩy divider xuống đúng vị trí: halfVisible items từ trên
            Spacer(Modifier.height(itemHeight * halfVisible))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f), thickness = 0.8.dp)
            // Khoảng cách giữa 2 divider = đúng 1 itemHeight (vùng chọn)
            Spacer(Modifier.height(itemHeight))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.25f), thickness = 0.8.dp)
        }
    }
}