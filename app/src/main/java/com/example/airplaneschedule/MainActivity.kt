package com.example.airplaneschedule

import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var switchEnabled: Switch
    private lateinit var editHour: EditText
    private lateinit var editMinute: EditText
    private lateinit var editDuration: EditText
    private lateinit var btnSave: Button
    private lateinit var txtStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchEnabled = findViewById(R.id.switchEnabled)
        editHour = findViewById(R.id.editHour)
        editMinute = findViewById(R.id.editMinute)
        editDuration = findViewById(R.id.editDuration)
        btnSave = findViewById(R.id.btnSave)
        txtStatus = findViewById(R.id.txtStatus)

        val prefs = ScheduleUtil.prefs(this)
        switchEnabled.isChecked = prefs.getBoolean(ScheduleUtil.KEY_ENABLED, false)
        editHour.setText(prefs.getInt(ScheduleUtil.KEY_HOUR, 0).toString())
        editMinute.setText(prefs.getInt(ScheduleUtil.KEY_MINUTE, 10).toString())
        editDuration.setText(prefs.getInt(ScheduleUtil.KEY_DURATION_MIN, 3).toString())

        updateStatusText()

        btnSave.setOnClickListener { onSave() }
    }

    private fun onSave() {
        val hour = editHour.text.toString().toIntOrNull() ?: 0
        val minute = editMinute.text.toString().toIntOrNull() ?: 10
        val duration = editDuration.text.toString().toIntOrNull() ?: 3
        val enabled = switchEnabled.isChecked

        if (hour !in 0..23 || minute !in 0..59 || duration <= 0) {
            Toast.makeText(this, "Проверь введённые значения", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, реально ли есть разрешение WRITE_SECURE_SETTINGS
        if (enabled) {
            val hasPermission = checkCallingOrSelfPermission(
                "android.permission.WRITE_SECURE_SETTINGS"
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Toast.makeText(
                    this,
                    "Нет разрешения WRITE_SECURE_SETTINGS. Выдай его через adb (см. README) и попробуй снова.",
                    Toast.LENGTH_LONG
                ).show()
                switchEnabled.isChecked = false
                return
            }
        }

        val prefs = ScheduleUtil.prefs(this)
        prefs.edit()
            .putBoolean(ScheduleUtil.KEY_ENABLED, enabled)
            .putInt(ScheduleUtil.KEY_HOUR, hour)
            .putInt(ScheduleUtil.KEY_MINUTE, minute)
            .putInt(ScheduleUtil.KEY_DURATION_MIN, duration)
            .apply()

        if (enabled) {
            ScheduleUtil.scheduleDailyOn(this, hour, minute)
            Toast.makeText(this, "Запланировано на $hour:$minute", Toast.LENGTH_SHORT).show()
        } else {
            ScheduleUtil.cancelAll(this)
            Toast.makeText(this, "Функция выключена", Toast.LENGTH_SHORT).show()
        }

        updateStatusText()
    }

    private fun updateStatusText() {
        val prefs = ScheduleUtil.prefs(this)
        val enabled = prefs.getBoolean(ScheduleUtil.KEY_ENABLED, false)
        txtStatus.text = if (enabled) {
            "Статус: активно, авиарежим включится в ${prefs.getInt(ScheduleUtil.KEY_HOUR, 0)}:${
                prefs.getInt(ScheduleUtil.KEY_MINUTE, 10).toString().padStart(2, '0')
            } на ${prefs.getInt(ScheduleUtil.KEY_DURATION_MIN, 3)} мин."
        } else {
            "Статус: выключено"
        }
    }
}
