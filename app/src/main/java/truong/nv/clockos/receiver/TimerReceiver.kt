package truong.nv.clockos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import truong.nv.clockos.service.TimerService

class TimerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Tín hiệu đã đến! Nhận được cái "lay người" từ hệ thống.

        // 1. Kích hoạt Service để bật nhạc và rung
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            // Báo cho Service biết là "Hết giờ rồi, kêu lên đi!"
            action = TimerService.ACTION_TIME_UP
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // (Tùy chọn) 2. Bạn có thể gọi thêm code hiển thị Notification đè màn hình khóa ở đây
        // để người dùng thấy nút bấm "Tắt". Tôi đã hướng dẫn phần Full-Screen Intent ở trên.
    }
}