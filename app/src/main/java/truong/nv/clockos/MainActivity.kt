package truong.nv.clockos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import truong.nv.clockos.service.TimerService
import truong.nv.clockos.ui.navigation.AppNavHost
import truong.nv.clockos.ui.theme.ClockOSTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Không cần xử lý gì thêm — nếu user từ chối thì notification sẽ không hiện
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Xin quyền hiển thị notification trên Android 13+ (API 33)
        requestNotificationPermission()

        setContent {
            ClockOSTheme {
                AppNavHost()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}

fun Context.sendCommandToTimerService(action: String, timeInMillis: Long = 0L) {
    val intent = Intent(this, TimerService::class.java).apply {
        this.action = action
        if (timeInMillis > 0) {
            putExtra(TimerService.EXTRA_TIME_IN_MS, timeInMillis)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }
}