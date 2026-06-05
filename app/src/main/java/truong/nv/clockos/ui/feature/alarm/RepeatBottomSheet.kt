package truong.nv.clockos.ui.feature.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.ui.theme.IosColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatBottomSheet(
    selectedDays: List<Int>,
    onDayToggled: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val days = listOf(
        Pair(1, "Mọi Thứ Hai"),
        Pair(2, "Mọi Thứ Ba"),
        Pair(3, "Mọi Thứ Tư"),
        Pair(4, "Mọi Thứ Năm"),
        Pair(5, "Mọi Thứ Sáu"),
        Pair(6, "Mọi Thứ Bảy"),
        Pair(7, "Mọi Chủ Nhật")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IosColor.DarkBackgroundSecondary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Lặp lại",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)

            LazyColumn {
                items(days) { (dayValue, dayName) ->
                    val isSelected = selectedDays.contains(dayValue)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDayToggled(dayValue) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayName,
                            color = Color.White,
                            fontSize = 17.sp
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = IosColor.Orange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}
