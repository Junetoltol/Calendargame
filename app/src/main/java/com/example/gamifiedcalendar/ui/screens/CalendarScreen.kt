package com.example.gamifiedcalendar.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gamifiedcalendar.data.calender.CalenderEvent
import com.example.gamifiedcalendar.data.calender.CalendarProviderDataSource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen() {
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(hasCalendarPermission(context)) }
    var deniedPermanently by remember { mutableStateOf(false) }

    // 조회할 날짜(일단 "오늘"로 고정 — 달력 UI 붙이면 이 값을 바꾸면 됨)
    var selectedDayMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    var events by remember { mutableStateOf<List<CalenderEvent>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            deniedPermanently = isDeniedPermanently(context)
        }
    }

    LaunchedEffect(hasPermission, selectedDayMillis) {
        if (!hasPermission) return@LaunchedEffect
        try {
            val (startMillis, endMillis) = dayRangeMillis(selectedDayMillis)
            val ds = CalendarProviderDataSource(context.contentResolver)
            events = ds.getEventsBetween(startMillis, endMillis)
            errorMsg = null
        } catch (e: Exception) {
            errorMsg = e.message
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Calendar", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (!hasPermission) {
            Text("캘린더 일정을 표시하려면 권한이 필요합니다.")
            Spacer(Modifier.height(12.dp))

            Button(onClick = { launcher.launch(Manifest.permission.READ_CALENDAR) }) {
                Text("캘린더 권한 허용")
            }

            if (deniedPermanently) {
                Spacer(Modifier.height(12.dp))
                Text("권한을 더 이상 요청할 수 없습니다. 설정에서 캘린더 권한을 허용해 주세요.")
            }
            return@Column
        }

        Text("선택 날짜: ${formatDay(selectedDayMillis)}")
        Spacer(Modifier.height(12.dp))

        when {
            errorMsg != null -> Text("오류: $errorMsg")
            events.isEmpty() -> Text("이 날짜에는 일정이 없습니다.")
            else -> events.forEach { ev ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(ev.title.ifBlank { "(제목 없음)" })
                        Text("begin=${ev.begin} / end=${ev.end}")
                        if (!ev.location.isNullOrBlank()) Text("loc=${ev.location}")
                    }
                }
            }
        }
    }
}

private fun hasCalendarPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

private fun isDeniedPermanently(context: Context): Boolean {
    val activity = context as? Activity ?: return false
    // "다시 묻지 않음" 케이스에서 false가 나오는 것이 핵심
    return !ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        Manifest.permission.READ_CALENDAR
    )
}

/** selectedDayMillis가 속한 날짜의 [00:00, 다음날 00:00) 범위(ms) 반환 */
private fun dayRangeMillis(selectedDayMillis: Long): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selectedDayMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.DAY_OF_MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}

private fun formatDay(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(millis)
}

