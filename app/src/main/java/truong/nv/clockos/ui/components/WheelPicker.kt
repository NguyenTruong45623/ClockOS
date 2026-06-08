package truong.nv.clockos.ui.components

import android.R.attr.top
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 50.dp,
    visibleCount: Int = 5,
    onSelected: (String) -> Unit = {}
) {

    require(visibleCount % 2 == 1)

    val centerIndex = visibleCount / 2

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = 0
    )

    val flingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState
    )

    val itemHeightPx = with(LocalDensity.current) {
        itemHeight.toPx()
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex
        }.collect { index ->
            items.getOrNull(index + centerIndex)?.let {
                onSelected(it)
            }
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleCount)
    ) {

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(
                vertical = itemHeight * centerIndex
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            itemsIndexed(
                items = items,
                key = { index, item ->
                    "$index-$item"
                }
            ) { _, item ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .wheelItemTransformation(
                            listState = listState,
                            itemHeightPx = itemHeightPx
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = item,
                        fontSize = 22.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .border(
                    width = 1.dp,
                    color = Color.Gray
                )
        )
    }
}

fun Modifier.wheelItemTransformation(
    listState: LazyListState,
    itemHeightPx: Float
): Modifier = composed {

    val layoutInfo = listState.layoutInfo

    graphicsLayer {

        val visibleItems = layoutInfo.visibleItemsInfo

        val currentItem =
            visibleItems.firstOrNull {
                it.offset <= top.toInt() &&
                        it.offset + it.size >= top.toInt()
            }

        currentItem?.let {

            val viewportCenter =
                layoutInfo.viewportEndOffset / 2f

            val itemCenter =
                it.offset + it.size / 2f

            val distance =
                itemCenter - viewportCenter

            val normalized =
                (distance / itemHeightPx)
                    .coerceIn(-3f, 3f)

            rotationX = normalized * -25f

            scaleX =
                (1f - kotlin.math.abs(normalized) * 0.08f)
                    .coerceAtLeast(0.75f)

            scaleY =
                (1f - kotlin.math.abs(normalized) * 0.08f)
                    .coerceAtLeast(0.75f)

            alpha =
                (1f - kotlin.math.abs(normalized) * 0.25f)
                    .coerceAtLeast(0.2f)

            cameraDistance = 32f * density
        }
    }
}