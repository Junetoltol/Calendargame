package com.example.gamifiedcalendar.data.calendar

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CalendarPermissionManager {

    fun hasReadCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** 사용자가 "다시 묻지 않음"을 체크했는지 판단 (Activity가 없으면 false) */
    fun isDeniedPermanently(context: Context): Boolean {
        val activity = context.findActivity() ?: return false
        return !ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.READ_CALENDAR
        )
    }

    private fun Context.findActivity(): Activity? {
        var ctx: Context = this
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
