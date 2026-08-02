package com.example.airplaneschedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import java.util.Calendar

object ScheduleUtil {

    const val PREFS = "airplane_schedule_prefs"
    const val KEY_ENABLED = "enabled"
    const val KEY_HOUR = "hour"
    const val KEY_MINUTE = "minute"
    const val KEY_DURATION_MIN = "duration_min"

    const val ACTION_ON = "com.example.airplaneschedule.ACTION_ON"
    const val ACTION_OFF = "com.example.airplaneschedule.ACTION_OFF"

    private const val REQ_ON = 1001
    private const val REQ_OFF = 1002

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Пытается включить/выключить авиарежим. Возвращает true при успехе. */
    fun setAirplaneMode(context: Context, enable: Boolean): Boolean {
        return try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                if (enable) 1 else 0
            )
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            intent.putExtra("state", enable)
            context.sendBroadcast(intent)
            true
        } catch (e: SecurityException) {
            // Значит разрешение WRITE_SECURE_SETTINGS не выдано через adb
            Log.e("ScheduleUtil", "Нет разрешения WRITE_SECURE_SETTINGS", e)
            false
        }
    }

    /** Планирует ближайший ежедневный будильник включения авиарежима. */
    fun scheduleDailyOn(context: Context, hour: Int, minute: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val intent = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_ON }
        val pi = PendingIntent.getBroadcast(
            context, REQ_ON, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }

    /** Планирует однократный будильник выключения через duration минут. */
    fun scheduleOff(context: Context, durationMin: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + durationMin * 60_000L
        val intent = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_OFF }
        val pi = PendingIntent.getBroadcast(
            context, REQ_OFF, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentOn = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_ON }
        val intentOff = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_OFF }
        am.cancel(
            PendingIntent.getBroadcast(
                context, REQ_ON, intentOn,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        am.cancel(
            PendingIntent.getBroadcast(
                context, REQ_OFF, intentOff,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}
