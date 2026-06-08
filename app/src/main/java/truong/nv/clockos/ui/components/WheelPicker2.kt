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
import androidx.compose.ui.tooling.preview.Preview
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
    velocityMultiplier: Float = 2.5f,
    frictionMultiplier: Float = 0.5f,
    snapEarlyThreshold: Float = 150f,
    rotationXMax: Float = 85f,
    scaleDownRatio: Float = 0.5f,
    cameraDistanceDensity: Float = 6f,
    curveMultiplier: Float = 1.8f, // Tăng mạnh hệ số cong để hút sát các item ở rìa vào
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
                val velocity = -velocityTracker.calculateVelocity().y * velocityMultiplier

                scope.launch {
                    // Phase 1: Decay — momentum tự nhiên kiểu iOS
                    val decayJob = launch {
                        scrollPx.animateDecay(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = frictionMultiplier)
                        )
                    }

                    // Liên tục theo dõi vận tốc, khi vòng quay chậm lại, lập tức dừng decay sớm để "hút" (snap) vào item gần nhất.
                    while (decayJob.isActive) {
                        if (scrollPx.velocity.absoluteValue < snapEarlyThreshold && scrollPx.velocity.absoluteValue > 0f) {
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

                            // Cylinder Projection: Kéo các item ở xa tâm (rìa) xích lại gần tâm hơn
                            // Tạo hiệu ứng cong thành cung tròn thật thay vì chỉ xoay phẳng
                            val curveTranslationY =
                                -(fraction * absFraction) * curveMultiplier * itemHeightPx

                            // Dịch chuyển cuộn mượt + dịch chuyển cong 3D
                            translationY = -fractional + curveTranslationY

                            // ── iOS UIPickerView Effects ──

                            // Alpha: giữa = 1.0, rìa = ~0.45 (mờ nhưng vẫn đọc được)
                            alpha = 1f - absFraction * 0.55f

                            // Cylinder 3D: xoay quanh trục X tạo hiệu ứng trống xoay
                            rotationX = fraction * rotationXMax

                            // Tăng cường 3D: thu nhỏ item khi ra rìa
                            val scale = 1f - absFraction * scaleDownRatio
                            scaleX = scale
                            scaleY = scale

                            // Camera distance: phối cảnh ống kính
                            cameraDistance = cameraDistanceDensity * density.density
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

@Preview(
    showBackground = true,
    backgroundColor = 0xFF1C1C1E
)
@Composable
fun WheelPicker2Preview() {
    val values =
        (0..59).map {
            it.toString().padStart(2, '0')
        }
    WheelPicker2(
        values = values,
        visibleItemsCount = 9,
        itemHeight = 23.dp,
        startIndex = 0,
        textSize = 18.sp
    ) { _, item ->
        // Callback item đã chọn
    }

}