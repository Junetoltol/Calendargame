package com.example.gamifiedcalendar.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gamifiedcalendar.MainActivity
import com.example.gamifiedcalendar.data.calendar.CalendarEvent
import com.example.gamifiedcalendar.data.calendar.CalendarPermissionManager
import com.example.gamifiedcalendar.data.calendar.CalendarRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen() {
    val context = LocalContext.current

    // 권한 매니저
    val permissionManager = remember { CalendarPermissionManager() }
    var hasPermission by remember {
        mutableStateOf(permissionManager.hasReadCalendarPermission(context))
    }
    var deniedPermanently by remember { mutableStateOf(false) }

    // 선택 날짜(ms) + 현재 표시 중인 "월"(그리드 기준)
    var selectedDayMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var currentMonthMillis by remember { mutableStateOf(startOfMonth(selectedDayMillis)) }

    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            deniedPermanently = permissionManager.isDeniedPermanently(context)
        }
    }

    // Repository 기반 조회
    LaunchedEffect(hasPermission, selectedDayMillis) {
        if (!hasPermission) return@LaunchedEffect
        try {
            val repo = CalendarRepository(context.contentResolver)
            events = repo.getEventsForDay(selectedDayMillis)
            errorMsg = null
        } catch (e: Exception) {
            errorMsg = e.message
        }
    }

    // ===== 6단계: 오늘 ToDo 요약(더미) =====
    // TODO: ToDo DB가 붙으면 TodaySummaryRepository 내부만 교체하면 됨
    val summaryRepo = remember { TodaySummaryRepository() }
    val todaySummary = remember { summaryRepo.getTodaySummary() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        // ===== 상단 헤더 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calendar", style = MaterialTheme.typography.titleLarge)

            TextButton(onClick = {
                val now = System.currentTimeMillis()
                selectedDayMillis = now
                currentMonthMillis = startOfMonth(now)
            }) { Text("오늘") }
        }

        Spacer(Modifier.height(10.dp))

        // ===== 6단계: 오늘 요약 카드(앱 화면 + 위젯과 동일 데이터 모델) =====
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("오늘 할 일", style = MaterialTheme.typography.titleMedium)
                    Text("${todaySummary.progressPercent}%", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(8.dp))

                todaySummary.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 요약은 "읽기 전용"으로 두는 게 일반적 (위젯/요약 카드 공용)
                        Checkbox(checked = item.done, onCheckedChange = null)
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(item.title)
                            item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                Text(desc, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ===== 월 이동 바 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                currentMonthMillis = addMonths(currentMonthMillis, -1)
                selectedDayMillis = currentMonthMillis
            }) { Text("◀") }

            Text(
                text = formatMonthYear(currentMonthMillis),
                style = MaterialTheme.typography.titleMedium
            )

            TextButton(onClick = {
                currentMonthMillis = addMonths(currentMonthMillis, 1)
                selectedDayMillis = currentMonthMillis
            }) { Text("▶") }
        }

        Spacer(Modifier.height(12.dp))

        // ===== 권한 섹션 =====
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

        // ===== 5단계: 월 달력 그리드 =====
        CalendarMonthGrid(
            monthMillis = currentMonthMillis,
            selectedDayMillis = selectedDayMillis,
            onSelectDay = { dayMillis ->
                selectedDayMillis = dayMillis
                currentMonthMillis = startOfMonth(dayMillis)
            }
        )

        Spacer(Modifier.height(12.dp))

        // ===== 선택 날짜 표시 =====
        Text(
            text = "선택 날짜: ${formatDayWithWeekday(selectedDayMillis)}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(12.dp))

        // ===== 일정 리스트 =====
        when {
            errorMsg != null -> Text("오류: $errorMsg")
            events.isEmpty() -> Text("이 날짜에는 일정이 없습니다.")
            else -> events.forEach { ev ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(ev.title.ifBlank { "(제목 없음)" })
                        Text("begin=${formatTime(ev.begin)} / end=${formatTime(ev.end)}")
                        if (!ev.location.isNullOrBlank()) Text("loc=${ev.location}")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    monthMillis: Long,
    selectedDayMillis: Long,
    onSelectDay: (Long) -> Unit
) {
    val yearMonth = remember(monthMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = monthMillis }
        cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }
    val year = yearMonth.first
    val month = yearMonth.second

    val firstDayOfMonth = remember(monthMillis) { firstDayOfMonth(year, month) }
    val daysInMonth = remember(monthMillis) { daysInMonth(year, month) }
    val leadingBlanks = remember(monthMillis) { leadingBlanksForSundayStart(firstDayOfMonth) }

    Row(Modifier.fillMaxWidth()) {
        listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    val totalCells = 42
    val dayNumbers = (0 until totalCells).map { idx ->
        val dayIndex = idx - leadingBlanks + 1
        if (dayIndex in 1..daysInMonth) dayIndex else null
    }

    Column {
        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val idx = row * 7 + col
                    val day = dayNumbers[idx]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day == null) {
                            Spacer(Modifier.size(1.dp))
                        } else {
                            val dayMillis = dayToMillis(year, month, day)
                            val isSelected = isSameDay(dayMillis, selectedDayMillis)
                            val isToday = isSameDay(dayMillis, System.currentTimeMillis())

                            DayCell(
                                day = day,
                                isSelected = isSelected,
                                isToday = isToday,
                                onClick = { onSelectDay(dayMillis) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isToday && !isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(4.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

/* ---------------------------
   6단계: 오늘 요약 모델/레포 (더미)
   --------------------------- */

private data class TodaySummary(
    val progressPercent: Int,
    val items: List<TodayTodoItem>
)

private data class TodayTodoItem(
    val title: String,
    val description: String? = null,
    val done: Boolean = false
)

private class TodaySummaryRepository {
    fun getTodaySummary(): TodaySummary {
        val items = listOf(
            TodayTodoItem(title = "Label", description = "Description", done = true),
            TodayTodoItem(title = "Label", description = "Description", done = false),
            TodayTodoItem(title = "Label", description = "Description", done = false),
        )
        val doneCount = items.count { it.done }
        val percent = if (items.isEmpty()) 0 else (doneCount * 100 / items.size)
        return TodaySummary(progressPercent = percent, items = items.take(3))
    }
}

/* ---------------------------
   7단계: Glance 위젯(같은 TodaySummaryRepository 재사용)
   파일은 보통 com.example.gamifiedcalendar.widget 로 분리하지만,
   "한 번에 붙여넣기" 위해 여기서도 동작하도록 구성.
   --------------------------- */

@Suppress("unused") // receiver/manifest에서 참조
object WidgetEntryPoint {
    // 이 오브젝트는 “한 파일 복붙” 구성에서 위젯 클래스들이 Proguard/정리 대상이 되지 않게 최소 anchor 역할
}

// === 아래 2개 클래스는 "별도 파일로 분리"가 정석이지만, 지금은 한 파일 편의 버전 ===

/*
패키지를 manifest에서 ".widget.TodayTodoWidgetReceiver"로 잡았으니,
실제로는 아래 2개를 별도 파일로 옮겨야 한다:

package com.example.gamifiedcalendar.widget

... (TodayTodoWidgetReceiver, TodayTodoWidget)
*/

// 여기서는 컴파일 편의상 "내부 위젯 구현 코드"를 주석으로 제공한다.
// 실제 적용은 아래 "7단계 파일 2개" 섹션을 그대로 생성하면 된다.

/* ---------------------------
   달력 유틸
   --------------------------- */

private fun startOfMonth(millis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun firstDayOfMonth(year: Int, month0: Int): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun daysInMonth(year: Int, month0: Int): Int {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun leadingBlanksForSundayStart(firstDayMillis: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = firstDayMillis }
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    return dow - Calendar.SUNDAY
}

private fun dayToMillis(year: Int, month0: Int, day: Int): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month0)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun isSameDay(aMillis: Long, bMillis: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = aMillis }
    val b = Calendar.getInstance().apply { timeInMillis = bMillis }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
            a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH)
}

private fun addMonths(baseMillis: Long, months: Int): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = baseMillis }
    cal.add(Calendar.MONTH, months)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun formatMonthYear(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy년 M월", Locale.getDefault())
    return sdf.format(millis)
}

private fun formatDayWithWeekday(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.getDefault())
    return sdf.format(millis)
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(millis)
}
