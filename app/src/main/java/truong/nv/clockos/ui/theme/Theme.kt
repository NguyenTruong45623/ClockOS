package truong.nv.clockos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val IosDarkColorScheme = darkColorScheme(
    primary = IosColor.Orange,
    background = IosColor.DarkBackgroundPrimary,
    surface = IosColor.DarkBackgroundSecondary,
    onBackground = IosColor.DarkLabelPrimary,
    onSurface = IosColor.DarkLabelPrimary,
    onSurfaceVariant = IosColor.DarkLabelSecondary,
    error = IosColor.Red
)

private val IosLightColorScheme = lightColorScheme(
    primary = IosColor.OrangeLight,
    background = IosColor.LightBackgroundPrimary,
    surface = IosColor.LightBackgroundSecondary,
    onBackground = IosColor.LightLabelPrimary,
    onSurface = IosColor.LightLabelPrimary,
    onSurfaceVariant = IosColor.LightLabelSecondary,
    error = IosColor.RedLight
)

@Composable
fun ClockOSTheme(
    // Giữ lại biến này để hệ thống tự động đổi Light/Dark Mode theo máy
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Chỉ chọn 1 trong 2 bộ màu chuẩn iOS, không phụ thuộc vào Android 12+ (Dynamic Color)
    val colorScheme = if (darkTheme) IosDarkColorScheme else IosLightColorScheme
    val view = LocalView.current

    // Xử lý đổi màu thanh trạng thái (StatusBar) và thanh điều hướng (NavigationBar) chuẩn Edge-to-Edge
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            // Nếu là Light Mode thì icon hệ thống (pin, sóng) màu đen, Dark Mode thì icon màu trắng
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}