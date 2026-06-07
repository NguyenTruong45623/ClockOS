package truong.nv.clockos.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker2(
    items: List<String>,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5, // Nên là số lẻ
    itemHeight: Dp = 48.dp,
    startIndex: Int = 0,
    sizeText: TextUnit = 22.sp,
    onItemSelected: (index: Int, item: String) -> Unit
) {
    if (items.isEmpty()) return

    val halfVisibleItems = visibleItemsCount / 2

    // Tính toán index vô hạn để khi khởi tạo, item được chọn nằm chính giữa
    val baseIndex = (Int.MAX_VALUE / 2) / items.size * items.size
    val centerIndex = baseIndex + startIndex
    val firstVisibleIndex = centerIndex - halfVisibleItems

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = firstVisibleIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val centerOffset = layoutInfo.viewportSize.height / 2f

            val closestItem = layoutInfo.visibleItemsInfo.minByOrNull {
                val itemCenter = it.offset + (it.size / 2f)
                (itemCenter - centerOffset).absoluteValue
            }

            closestItem?.let {
                val realIndex = it.index % items.size
                onItemSelected(realIndex, items[realIndex])
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize()
            // Bỏ contentPadding vì bây giờ list cuộn vô hạn, luôn có item bên trên và dưới
        ) {
            items(Int.MAX_VALUE) { index ->
                val realIndex = index % items.size
                val item = items[realIndex]

                val alphaScaleRotation by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

                        if (itemInfo == null) {
                            Triple(0.2f, 0.8f, 60f) // alpha, scale, rotationX
                        } else {
                            val center = layoutInfo.viewportSize.height / 2f
                            val itemCenter = itemInfo.offset + (itemInfo.size / 2f)
                            val distanceFromCenter = itemCenter - center

                            val maxDistance = itemHeightPx * halfVisibleItems
                            // Fraction ranges from -1 (top) to 1 (bottom)
                            val fraction = (distanceFromCenter / maxDistance).coerceIn(-1f, 1f)

                            // iOS cylinder effect calculation
                            val rotationX = fraction * 60f // Rotate up to 60 degrees
                            val absFraction = fraction.absoluteValue
                            
                            val alpha = 0.2f + (0.8f * (1f - absFraction))
                            val scale = 0.85f + (0.15f * (1f - absFraction))

                            Triple(alpha, scale, rotationX)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            this.alpha = alphaScaleRotation.first
                            this.scaleX = alphaScaleRotation.second
                            this.scaleY = alphaScaleRotation.second
                            this.rotationX = alphaScaleRotation.third
                            
                            // Tùy chỉnh cameraDistance để hiệu ứng 3D trông chân thực hơn
                            this.cameraDistance = 8f * density.density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        fontSize = sizeText,
                        fontWeight = if (alphaScaleRotation.first > 0.8f) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        // Highlight box (giống iOS picker)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
        ) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.TopCenter),
                color = Color.Gray.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = Color.Gray.copy(alpha = 0.3f),
                thickness = 1.dp
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFF000000
)
@Composable
fun DemoScreen() {
    val options = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
    var selectedOption by remember { mutableStateOf(options.first()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Bạn đã chọn: $selectedOption", color = Color.White, modifier = Modifier.padding(16.dp))

        WheelPicker2(
            items = options,
            visibleItemsCount = 7,
            itemHeight = 30.dp,
            onItemSelected = { index, item ->
                selectedOption = item
            }
        )
    }
}