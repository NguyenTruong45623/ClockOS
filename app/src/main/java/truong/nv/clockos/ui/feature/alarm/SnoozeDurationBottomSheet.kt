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
fun SnoozeDurationBottomSheet(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val durations = listOf(
        Pair(3, "3 Phút"),
        Pair(5, "5 Phút"),
        Pair(9, "9 Phút"),
        Pair(10, "10 Phút"),
        Pair(15, "15 Phút"),
        Pair(30, "30 Phút")
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
                text = "Thời gian báo lại",
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
                items(durations) { (durationValue, durationName) ->
                    val isSelected = selectedDuration == durationValue

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDurationSelected(durationValue)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = durationName,
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
