package truong.nv.clockos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
fun ProgressRing(progress: Float) {
    val strokeColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(320.dp)) {
        val strokeWidthPx = 8.dp.toPx()
        drawCircle(color = strokeColor.copy(alpha = 0.08f), style = Stroke(width = strokeWidthPx))
        drawArc(
            color = strokeColor,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )
    }
}