package truong.nv.clockos.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun WheelPicker(items: List<Int>, label: String, onItemSelected: (Int) -> Unit) {
    val itemHeight = 45.dp
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val currentIndex = listState.firstVisibleItemIndex
            val scrollOffset = listState.firstVisibleItemScrollOffset
            val targetIndex = if (scrollOffset > itemHeight.value / 2) currentIndex + 1 else currentIndex
            if (targetIndex < items.size) {
                coroutineScope.launch { listState.animateScrollToItem(targetIndex) }
                onItemSelected(items[targetIndex])
            }
        }
    }

    Row(modifier = Modifier.height(itemHeight * 5), verticalAlignment = Alignment.CenterVertically) {
        LazyColumn(
            state = listState,
            modifier = Modifier.width(55.dp).height(itemHeight * 5),
            contentPadding = PaddingValues(vertical = itemHeight * 2)
        ) {
            items(items.size) { index ->
                Box(modifier = Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d", items[index]),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 23.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, modifier = Modifier.width(40.dp))
    }
}