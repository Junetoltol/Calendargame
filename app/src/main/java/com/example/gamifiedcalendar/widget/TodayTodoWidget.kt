package com.example.gamifiedcalendar.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.action.clickable
import androidx.glance.unit.dp
import com.example.gamifiedcalendar.MainActivity

class TodayTodoWidget : GlanceAppWidget() {

    private val repo = TodaySummaryRepository()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val summary = repo.load(context)

        provideContent {
            TodayTodoWidgetContent(
                context = context,
                summary = summary
            )
        }
    }
}

@Composable
private fun TodayTodoWidgetContent(
    context: Context,
    summary: TodaySummary
) {
    val openAppAction = actionStartActivity(
        ComponentName(context, MainActivity::class.java)
    )

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(onClick = openAppAction)
    ) {
        Text(text = "오늘 할 일  ${summary.progressPercent}%")
        Spacer(modifier = GlanceModifier.height(10.dp))

        if (summary.items.isEmpty()) {
            Text(text = "오늘 할 일이 없습니다.")
        } else {
            summary.items.forEach { item ->
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(text = if (item.done) "☑" else "☐")
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Text(text = item.title)
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}
