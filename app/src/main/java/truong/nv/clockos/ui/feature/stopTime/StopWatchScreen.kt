package truong.nv.clockos.ui.feature.stopTime

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import truong.nv.clockos.ui.components.ControlButton
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
annotation class StopWatchThemePreviews

@Composable
fun StopWatchScreen(
    navigator: AppNavigator? = null,
    viewModel: StopWatchViewModel = hiltViewModel()
) {

    val state = viewModel.state.collectAsStateWithLifecycle().value

    val mainTime = state.elapsedTime.toStopWatchText()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val observer = LifecycleEventObserver { _, event ->

            when (event) {

                Lifecycle.Event.ON_STOP -> {
                    viewModel.onAction(StopWatchEvent.OnAppBackground)
                }

                Lifecycle.Event.ON_START -> {
                    viewModel.onAction(StopWatchEvent.OnAppForeground)
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mainTime,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 85.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = (-1.5).sp,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            ControlButton(
                text = when {
                    state.isRunning -> "Vòng"
                    state.elapsedTime > 0 -> "Đặt lại"
                    else -> "Vòng"
                },
                backgroundColor = if (
                    state.isRunning || state.elapsedTime > 0
                ) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                },
                textColor = if (
                    state.isRunning || state.elapsedTime > 0
                ) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                enabled = state.isRunning || state.elapsedTime > 0,
                onClick = {
                    when {
                        state.isRunning ->
                            viewModel.onAction(StopWatchEvent.Lap)

                        state.elapsedTime > 0 ->
                            viewModel.onAction(StopWatchEvent.Reset)
                    }
                }
            )

            ControlButton(
                text = if (state.isRunning) {
                    "Dừng"
                } else {
                    if (state.elapsedTime > 0)
                        "Tiếp tục"
                    else
                        "Bắt đầu"
                },
                backgroundColor = if (state.isRunning) {
                    IosColor.Red.copy(alpha = 0.15f)
                } else {
                    IosColor.Green.copy(alpha = 0.15f)
                },
                textColor = if (state.isRunning) {
                    IosColor.Red
                } else {
                    IosColor.GreenLight
                },
                onClick = {
                    if (state.isRunning) {
                        viewModel.onAction(StopWatchEvent.Stop)
                    } else {
                        viewModel.onAction(StopWatchEvent.Start)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
        ) {

            if (state.laps.isNotEmpty()) {

                val fastestLap =
                    state.laps.minByOrNull { it.elapsedTime }

                val slowestLap =
                    state.laps.maxByOrNull { it.elapsedTime }

                Column {

                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                            .copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        itemsIndexed(
                            state.laps
                        ) { index, lap ->

                            val color = when {
                                state.laps.size >= 2 &&
                                        lap == fastestLap ->
                                    IosColor.Green

                                state.laps.size >= 2 &&
                                        lap == slowestLap ->
                                    IosColor.Red

                                else ->
                                    MaterialTheme
                                        .colorScheme
                                        .onBackground
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = "Vòng ${index+1}",
                                    color = color,
                                    fontSize = 17.sp
                                )

                                Text(
                                    text = lap.elapsedTime.toStopWatchText(),
                                    color = color,
                                    fontSize = 17.sp
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                                    .copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

fun Long.toStopWatchText(): String {

    val totalSeconds = this / 1000

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val centiseconds = (this % 1000) / 10

    return if (hours > 0) {
        String.format(
            "%02d:%02d:%02d.%02d",
            hours,
            minutes,
            seconds,
            centiseconds
        )
    } else {
        String.format(
            "%02d:%02d.%02d",
            minutes,
            seconds,
            centiseconds
        )
    }
}

@StopWatchThemePreviews
@Composable
private fun StopWatchScreenPreview() {
    MaterialTheme {
        StopWatchScreen()
    }
}