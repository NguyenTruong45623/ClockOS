package truong.nv.clockos.ui.feature.worldClock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import truong.nv.clockos.ui.components.WheelPicker2
import truong.nv.clockos.ui.navigation.AppNavigator

@Composable
fun WorldClockScreen(
    navigator: AppNavigator
) {
    Text("WorldClockScreen")

    val options = listOf("1","2", "3", "4", "5", "6","7","8", "9", "10")
    var selectedOption by remember { mutableStateOf(options.first()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Bạn đã chọn: $selectedOption", modifier = Modifier.padding(16.dp))

        WheelPicker2(
            items = options,
            visibleItemsCount = 5,
            onItemSelected = { index, item ->
                selectedOption = item
            }
        )
    }
}