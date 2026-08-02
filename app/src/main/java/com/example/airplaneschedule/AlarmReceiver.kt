package com.example.airplaneschedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = ScheduleUtil.prefs(context)
        val enabled = prefs.getBoolean(ScheduleUtil.KEY_ENABLED, false)
        if (!enabled) return // функция выключена переключателем — ничего не делаем

        val durationMin = prefs.getInt(ScheduleUtil.KEY_DURATION_MIN, 3)
        val hour = prefs.getInt(ScheduleUtil.KEY_HOUR, 0)
        val minute = prefs.getInt(ScheduleUtil.KEY_MINUTE, 10)

        when (intent.action) {
            ScheduleUtil.ACTION_ON -> {
                ScheduleUtil.setAirplaneMode(context, true)
                // запланировать выключение через N минут
                ScheduleUtil.scheduleOff(context, durationMin)
                // и сразу запланировать включение на завтра
                ScheduleUtil.scheduleDailyOn(context, hour, minute)
            }
            ScheduleUtil.ACTION_OFF -> {
                ScheduleUtil.setAirplaneMode(context, false)
            }
        }
    }
}
