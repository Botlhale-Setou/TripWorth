package com.tripworth.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etHourly = findViewById<EditText>(R.id.et_hourly_rate)
        val btnSave = findViewById<Button>(R.id.btn_save_prefs)
        val btnOverlay = findViewById<Button>(R.id.btn_overlay_settings)

        // Load saved pref
        val prefs = getSharedPreferences("driver_prefs", MODE_PRIVATE)
        etHourly.setText(prefs.getFloat("hourly_rate", 30.0f).toString())

        btnSave.setOnClickListener {
            val hr = etHourly.text.toString().toFloatOrNull() ?: 30.0f
            prefs.edit().putFloat("hourly_rate", hr).apply()
        }

        btnOverlay.setOnClickListener {
            // Open overlay permission
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }
}
