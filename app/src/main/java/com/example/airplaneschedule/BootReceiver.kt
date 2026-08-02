package com.example.airplaneschedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = ScheduleUtil.prefs(context)
            val enabled = prefs.getBoolean(ScheduleUtil.KEY_ENABLED, false)
            if (enabled) {
                val hour = prefs.getInt(ScheduleUtil.KEY_HOUR, 0)
                val minute = prefs.getInt(ScheduleUtil.KEY_MINUTE, 10)
                ScheduleUtil.scheduleDailyOn(context, hour, minute)
            }
        }
    }
}
