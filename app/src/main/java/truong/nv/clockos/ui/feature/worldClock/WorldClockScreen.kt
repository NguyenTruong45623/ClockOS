package truong.nv.clockos.ui.feature.worldClock

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.components.WheelPicker2
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorldClockScreen(
    navigator: AppNavigator
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // State quản lý việc hiển thị Dialog giải thích thông thường (Khi mới từ chối 1 lần)
    var showRationaleDialog by remember { mutableStateOf(false) }

    // State quản lý việc hiển thị Dialog điều hướng Cài đặt (Khi bị từ chối vĩnh viễn / Don't ask again)
    var showSettingsDialog by remember { mutableStateOf(false) }

    // 1. Khởi tạo Launcher nhận kết quả xin quyền từ hệ thống
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Đã cấp quyền thông báo!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bạn đã từ chối quyền thông báo.", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm điều hướng thẳng vào mục "Thông báo ứng dụng" của App trong Cài đặt hệ thống
    val openNotificationSettings = {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Điều hướng thẳng vào giao diện bật/tắt thông báo của riêng App (Android 8.0+)
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else {
                // Giao diện thông tin chi tiết App (Android cũ)
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
    }

    // DIALOG 1: Giải thích thông thường (Người dùng mới từ chối 1 lần, hệ thống vẫn cho xin lại)
    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text(text = "Cấp quyền thông báo") },
            text = { Text(text = "Ứng dụng cần quyền thông báo để gửi cho bạn những cập nhật quan trọng. Vui lòng cấp quyền.") },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // DIALOG 2: Yêu cầu vào Cài đặt (Khi người dùng chọn "Don't ask again" hoặc từ chối quá nhiều)
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(text = "Yêu cầu bật trong Cài đặt") },
            text = { Text(text = "Bạn đã tắt tính năng thông báo của ứng dụng. Vui lòng vào Cài đặt hệ thống để bật lại quyền này.") },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    openNotificationSettings() // Điều hướng đi
                }) {
                    Text("Đi đến Cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // 2. Giao diện chính với Nút bấm
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                )

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Quyền thông báo đã được bật trước đó.", Toast.LENGTH_SHORT).show()
                } else {
                    // Kiểm tra xem hệ thống có yêu cầu hiện Rationale không
                    val shouldShowRationale = activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
                    } ?: false

                    if (shouldShowRationale) {
                        // Trường hợp: Người dùng đã từ chối ít nhất 1 lần nhưng CHƯA tích "Don't ask again"
                        showRationaleDialog = true
                    } else {
                        // Trường hợp `shouldShowRationale == false` xảy ra ở 2 trạng thái:
                        // 1. Lần đầu tiên app bấm nút xin quyền (chưa từng bị từ chối)
                        // 2. Đã bị tích "Don't ask again" (Từ chối vĩnh viễn)

                        // Để phân biệt, ta có thể dùng một thủ thuật dựa trên SharedPreferences,
                        // hoặc dùng chính biến flag kiểm tra trạng thái để nhận diện.
                        // Cách đơn giản nhất: Gọi thử launcher, nếu là trường hợp (2) hệ thống sẽ từ chối ngay lập tức
                        // mà không hiện gì. Nhưng để UX tốt nhất (bấm phát hiện dialog Cài đặt luôn):

                        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        val isFirstTime = sharedPref.getBoolean("first_time_notification_ask", true)

                        if (isFirstTime) {
                            // Lần đầu tiên trong đời bấm nút -> Cho gọi popup hệ thống
                            sharedPref.edit().putBoolean("first_time_notification_ask", false).apply()
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Không phải lần đầu, mà shouldShowRationale lại bằng false -> Chắc chắn đã bấm "Don't ask again"
                            showSettingsDialog = true
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Android 12 trở xuống mặc định đã có quyền", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Yêu cầu bật thông báo")
        }
    }

//    val values =
//    (0..59).map {
//        it.toString().padStart(2, '0')
//    }
//
//    var selected by remember {
//        mutableStateOf(values.first())
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//

//        Text(
//            text = "Selected: $selected",
//            color = Color.White
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        WheelPicker2(
//            values = values,
//            visibleItemsCount = 9,
//            itemHeight = 30.dp,
//            startIndex = 0,
//            textSize = 18.sp
//        ) { _, item ->
//            selected = item
//        }
//
//        val items = remember {
//            List(1000) { index ->
//                "Item $index"
//            }
//        }
//
//        WheelPicker(
//            values = items,
//            visibleItemsCount = 9,
//            itemHeight = 40.dp,
//            startIndex = 0,
//            onItemSelected = { _, item ->
//                selected = item
//            }
//        )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CitySelectionSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    // Fake data nên được remember hoặc truyền từ ViewModel vào để tránh khởi tạo lại
    val cities = remember {
        listOf(
            "Abidjan",
            "Accra",
            "Addis Ababa",
            "Algiers",
            "Amman",
            "Amsterdam",
            "Athens",
            "Auckland",
            "Baghdad",
            "Bangkok",
            "Barcelona",
            "Beijing",
            "Beirut",
            "Berlin",
            "Bogota",
            "Boston",
            "Brisbane",
            "Brussels",
            "Bucharest",
            "Budapest",
            "Buenos Aires",
            "Cairo",
            "Cape Town",
            "Caracas",
            "Casablanca",
            "Chicago",
            "Copenhagen",
            "Dakar",
            "Dallas",
            "Damascus",
            "Delhi",
            "Denver",
            "Dhaka",
            "Dubai",
            "Dublin",
            "Edinburgh",
            "Edmonton",
            "Frankfurt",
            "Fukuoka",
            "Geneva",
            "Guatemala City",
            "Hanoi",
            "Havana",
            "Helsinki",
            "Ho Chi Minh City",
            "Hong Kong",
            "Honolulu",
            "Houston",
            "Istanbul",
            "Islamabad",
            "Jakarta",
            "Jerusalem",
            "Johannesburg",
            "Kabul",
            "Karachi",
            "Kathmandu",
            "Kiev",
            "Kuala Lumpur",
            "Kuwait City",
            "Lagos",
            "Lahore",
            "Las Vegas",
            "Lima",
            "Lisbon",
            "London",
            "Los Angeles",
            "Madrid",
            "Manila",
            "Melbourne",
            "Mexico City",
            "Miami",
            "Milan",
            "Montreal",
            "Moscow",
            "Mumbai",
            "Nairobi",
            "New Delhi",
            "New York",
            "Osaka",
            "Oslo",
            "Paris",
            "Perth",
            "Philadelphia",
            "Phoenix",
            "Prague",
            "Qatar",
            "Reykjavik",
            "Rio de Janeiro",
            "Rome",
            "Riyadh",
            "San Francisco",
            "Santiago",
            "Sao Paulo",
            "Seoul",
            "Shanghai",
            "Singapore",
            "Stockholm",
            "Sydney",
            "Taipei",
            "Tehran",
            "Tel Aviv",
            "Tokyo",
            "Toronto",
            "Vancouver",
            "Vienna",
            "Warsaw",
            "Washington",
            "Yangon",
            "Zurich"
        ).groupBy { it.first().uppercase() }.toSortedMap()
    }

    var searchQuery by remember { mutableStateOf("") }

    // Tối ưu: Chỉ tính toán lại khi searchQuery thay đổi.
    // Tối ưu thuật toán: Giữ nguyên cấu trúc Map, chỉ filter các list con, tiết kiệm memory.
    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            cities
        } else {
            cities.mapValues { (_, cityList) ->
                cityList.filter { it.contains(searchQuery, ignoreCase = true) }
            }.filterValues { it.isNotEmpty() }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1C1C1E),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chọn Thành Phố",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ), // Bỏ .height(50.dp) để tránh lỗi font bị cắt
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    // cursorColor = IosColor.Orange // Đảm bảo bạn đã define màu này
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true // Thêm dòng này để keyboard hoạt động đúng với UI Search
            )
        }

        // Box bọc ngoài LazyColumn để thêm hiệu ứng mờ dần (fade) ở 2 đầu nếu thích,
        // và xử lý clearFocus khi người dùng cuộn danh sách.
        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    // Chạm vào vùng trống (nếu có) cũng sẽ đóng bàn phím
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            val lazyListState = rememberLazyListState()

            // Đóng bàn phím khi user bắt đầu cuộn
            LaunchedEffect(lazyListState.isScrollInProgress) {
                if (lazyListState.isScrollInProgress) {
                    focusManager.clearFocus()
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize()
            ) {
                filteredCities.forEach { (letter, cityList) ->
                    stickyHeader(key = "header_$letter") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2E))
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 6.dp
                                ) // Chỉnh lại padding cho đẹp
                        ) {
                            Text(
                                text = letter,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    items(
                        items = cityList,
                        key = { city -> city } // Sử dụng trực tiếp city làm key
                    ) { city ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    // TODO: handle selection logic here
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = city,
                                color = Color.White,
                                fontSize = 17.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            // Sử dụng HorizontalDivider để đường line sắc nét hơn
                            HorizontalDivider(
                                color = Color.DarkGray.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 16.dp) // Không padding end để giống iOS
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}