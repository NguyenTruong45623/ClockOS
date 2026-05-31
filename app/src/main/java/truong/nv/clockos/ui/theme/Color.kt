package truong.nv.clockos.ui.theme

import androidx.compose.ui.graphics.Color

object IosColor {
    // --- 1. MÀU SẮC CHỦ ĐẠO (ACCENT / TINT COLORS) ---
    val Orange = Color(0xFFFF9F0A)       // Màu cam hẹn giờ, đồng hồ bấm giờ
    val OrangeLight = Color(0xFFFF9500)  // Cam phiên bản Light Mode
    val Green = Color(0xFF30D158)        // Xanh lá nút Bắt đầu (Dark)
    val GreenLight = Color(0xFF34C759)   // Xanh lá nút Bắt đầu (Light)
    val Red = Color(0xFFFF453A)          // Màu đỏ báo động, xóa (Dark)
    val RedLight = Color(0xFFFF3B30)     // Màu đỏ (Light)
    val Blue = Color(0xFF0A84FF)         // Màu xanh dương liên kết hệ thống (Dark)
    val BlueLight = Color(0xFF007AFF)    // Màu xanh dương (Light)

    // --- 2. HỆ THỐNG MÀU TỐI (DARK MODE THEME) ---
    val DarkBackgroundPrimary = Color(0xFF000000)   // Nền thuần đen (System Background)
    val DarkBackgroundSecondary = Color(0xFF1C1C1E) // Nền thẻ, hàng cấu hình (Grouped Background)
    val DarkBackgroundTertiary = Color(0xFF2C2C2E)  // Nền các thành phần nhỏ hơn

    val DarkLabelPrimary = Color(0xFFFFFFFF)        // Chữ chính (Trắng thuần)
    val DarkLabelSecondary = Color(0x99EBEBF5)      // Chữ phụ (Vibrant - Trắng mờ 60%)
    val DarkLabelTertiary = Color(0x4DEBEBF5)       // Chữ gợi ý / Hint (Trắng mờ 30%)

    // --- 3. HỆ THỐNG MÀU SÁNG (LIGHT MODE THEME) ---
    val LightBackgroundPrimary = Color(0xFFFFFFFF)  // Nền trắng thuần
    val LightBackgroundSecondary = Color(0xFFF2F2F7) // Nền xám nhạt cho thẻ/khối
    val LightBackgroundTertiary = Color(0xFFFFFFFF)

    val LightLabelPrimary = Color(0xFF000000)       // Chữ chính (Đen thuần)
    val LightLabelSecondary = Color(0x993C3C43)     // Chữ phụ (Đen mờ 60%)
    val LightLabelTertiary = Color(0x4D3C3C43)      // Chữ gợi ý / Hint (Đen mờ 30%)

    // --- 4. CÁC NÚT BẤM ĐẶC TRƯNG ---
    val DarkButtonButtonGray = Color(0xFF2C2C2E)    // Nút Hủy mờ (iOS Dark)
    val LightButtonButtonGray = Color(0xFFE5E5EA)   // Nút Hủy mờ (iOS Light)
}