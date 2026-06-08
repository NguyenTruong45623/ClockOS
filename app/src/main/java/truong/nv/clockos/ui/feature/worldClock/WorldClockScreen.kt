package truong.nv.clockos.ui.feature.worldClock

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import truong.nv.clockos.ui.components.WheelPicker
import truong.nv.clockos.ui.components.WheelPicker2
import truong.nv.clockos.ui.navigation.AppNavigator
import truong.nv.clockos.ui.theme.IosColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorldClockScreen(
    navigator: AppNavigator
) {
    val values =
    (0..59).map {
        it.toString().padStart(2, '0')
    }

    var selected by remember {
        mutableStateOf(values.first())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Selected: $selected",
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        WheelPicker2(
            values = values,
            visibleItemsCount = 9,
            itemHeight = 30.dp,
            startIndex = 0,
            textSize = 18.sp
        ) { _, item ->
            selected = item
        }

        val items = remember {
            List(1000) { index ->
                "Item $index"
            }
        }

        WheelPicker(
            values = items,
            visibleItemsCount = 9,
            itemHeight = 40.dp,
            startIndex = 0,
            onItemSelected = { _, item ->
                selected = item
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CitySelectionSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    // Fake data nên được remember hoặc truyền từ ViewModel vào để tránh khởi tạo lại
    val cities = remember {
        listOf(
            "Abidjan", "Accra", "Addis Ababa", "Algiers", "Amman", "Amsterdam", "Athens", "Auckland",
            "Baghdad", "Bangkok", "Barcelona", "Beijing", "Beirut", "Berlin", "Bogota", "Boston", "Brisbane", "Brussels", "Bucharest", "Budapest", "Buenos Aires",
            "Cairo", "Cape Town", "Caracas", "Casablanca", "Chicago", "Copenhagen",
            "Dakar", "Dallas", "Damascus", "Delhi", "Denver", "Dhaka", "Dubai", "Dublin",
            "Edinburgh", "Edmonton",
            "Frankfurt", "Fukuoka",
            "Geneva", "Guatemala City",
            "Hanoi", "Havana", "Helsinki", "Ho Chi Minh City", "Hong Kong", "Honolulu", "Houston",
            "Istanbul", "Islamabad",
            "Jakarta", "Jerusalem", "Johannesburg",
            "Kabul", "Karachi", "Kathmandu", "Kiev", "Kuala Lumpur", "Kuwait City",
            "Lagos", "Lahore", "Las Vegas", "Lima", "Lisbon", "London", "Los Angeles",
            "Madrid", "Manila", "Melbourne", "Mexico City", "Miami", "Milan", "Montreal", "Moscow", "Mumbai",
            "Nairobi", "New Delhi", "New York",
            "Osaka", "Oslo",
            "Paris", "Perth", "Philadelphia", "Phoenix", "Prague",
            "Qatar",
            "Reykjavik", "Rio de Janeiro", "Rome", "Riyadh",
            "San Francisco", "Santiago", "Sao Paulo", "Seoul", "Shanghai", "Singapore", "Stockholm", "Sydney",
            "Taipei", "Tehran", "Tel Aviv", "Tokyo", "Toronto",
            "Vancouver", "Vienna",
            "Warsaw", "Washington",
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
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Bỏ .height(50.dp) để tránh lỗi font bị cắt
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
                                .padding(horizontal = 16.dp, vertical = 6.dp) // Chỉnh lại padding cho đẹp
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