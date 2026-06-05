package truong.nv.clockos.ui.feature.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import truong.nv.clockos.service.AlarmRingingService
import truong.nv.clockos.ui.theme.ClockOSTheme
import truong.nv.clockos.ui.theme.IosColor
import java.util.Calendar

class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request keyguard and window flags to show over lock screen
        turnScreenOnAndShowOnLockScreen()

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val hour = intent.getIntExtra("ALARM_HOUR", 0)
        val minute = intent.getIntExtra("ALARM_MINUTE", 0)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Báo thức"

        setContent {
            ClockOSTheme(darkTheme = true) { // Always show dark theme for ringing screen
                AlarmRingingScreen(
                    alarmId = alarmId,
                    alarmTimeStr = String.format("%02d:%02d", hour, minute),
                    label = label,
                    onSnooze = {
                        sendActionToService("ACTION_SNOOZE", alarmId, hour, minute, label)
                        finish()
                    },
                    onDismiss = {
                        sendActionToService("ACTION_DISMISS", alarmId, hour, minute, label)
                        finish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndShowOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    private fun sendActionToService(action: String, alarmId: Int, hour: Int, minute: Int, label: String) {
        val serviceIntent = Intent(this, AlarmRingingService::class.java).apply {
            this.action = action
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra("ALARM_LABEL", label)
        }
        startService(serviceIntent)
    }
}

@Composable
fun AlarmRingingScreen(
    alarmId: Int,
    alarmTimeStr: String,
    label: String,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (isActive) {
            val cal = Calendar.getInstance()
            currentTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Time and Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = currentTime,
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = (-2).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label.uppercase(),
                color = IosColor.DarkLabelSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        // Center section: Huge Snooze Button
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(IosColor.Orange)
                .clickable { onSnooze() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LẶP LẠI",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Bottom section: Sleek Dismiss Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tắt báo thức",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
