package truong.nv.clockos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IosTimerPicker(
    onTimerSelected: (hour: Int, minute: Int, second: Int) -> Unit
) {
    // 1. Chuẩn bị dữ liệu cho từng vòng
    val hours = remember { (0..23).map { it.toString() } }
    val minutes = remember { (0..59).map { it.toString() } }
    val seconds = remember { (0..59).map { it.toString() } }

    // 2. Lưu trạng thái thời gian hiện tại
    // FIX: Dùng State object trực tiếp (không dùng `by` delegation)
    // để capture trong remember lambda một cách ổn định, tránh tạo lambda mới mỗi recomposition.
    val selectedHour = remember { mutableStateOf(0) }
    val selectedMinute = remember { mutableStateOf(0) }
    val selectedSecond = remember { mutableStateOf(0) }

    // FIX: Wrap callback bên ngoài bằng rememberUpdatedState
    // để lambda ổn định bên trong luôn gọi được phiên bản mới nhất của onTimerSelected.
    val currentOnTimerSelected by rememberUpdatedState(onTimerSelected)

    // FIX: Lambda ổn định — tạo 1 lần duy nhất, không đổi giữa các recomposition.
    // Cách cũ tạo lambda MỚI mỗi recomposition (do capture biến by-delegation thay đổi)
    // → WheelPicker2 nhận parameter khác → buộc recompose toàn bộ cây con (3 picker cùng lúc).
    val hourCallback = remember<(Int, String) -> Unit> {
        { index, _ ->
            selectedHour.value = index
            currentOnTimerSelected(selectedHour.value, selectedMinute.value, selectedSecond.value)
        }
    }
    val minuteCallback = remember<(Int, String) -> Unit> {
        { index, _ ->
            selectedMinute.value = index
            currentOnTimerSelected(selectedHour.value, selectedMinute.value, selectedSecond.value)
        }
    }
    val secondCallback = remember<(Int, String) -> Unit> {
        { index, _ ->
            selectedSecond.value = index
            currentOnTimerSelected(selectedHour.value, selectedMinute.value, selectedSecond.value)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {

        // --- ĐÂY LÀ PHẦN HIỂN THỊ CHỮ "giờ" "phút" "giây" ĐỨNG YÊN (Giống iOS) ---
        // Chúng ta vẽ một hàng chữ nằm đè lên chính giữa, dùng khoảng cách (Spacer) để căn chỉnh đúng vị trí
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(25.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột Giờ (Chiếm 1/3 độ rộng)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("giờ", color = Color.White, modifier = Modifier.padding(start = 50.dp))
            }
            // Cột Phút (Chiếm 1/3 độ rộng)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("phút", color = Color.White, modifier = Modifier.padding(start = 65.dp))
            }
            // Cột Giây (Chiếm 1/3 độ rộng)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("giây", color = Color.White, modifier = Modifier.padding(start = 65.dp))
            }
        }

        // --- LỚP DƯỚI: 3 WHEEL PICKER ĐẶT CẠNH NHAU ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vòng chọn Giờ
//            WheelPicker2(
//                values = hours,
//                modifier = Modifier.weight(1f),
//                startIndex = 0,
//                onItemSelected = hourCallback
//            )
//
//            // Vòng chọn Phút
//            WheelPicker2(
//                values = minutes,
//                modifier = Modifier.weight(1f),
//                startIndex = 0,
//                onItemSelected = minuteCallback
//            )
//
//            // Vòng chọn Giây
//            WheelPicker2(
//                values = seconds,
//                modifier = Modifier.weight(1f),
//                startIndex = 0,
//                onItemSelected = secondCallback
//            )
        }
    }
}

@Preview
@Composable
fun IosTimerPickerPreview() {
    IosTimerPicker(onTimerSelected = { hour, minute, second ->
        // Xử lý khi người dùng chọn giờ, phút, giây
    })
}