// MainActivity.kt
package com.example.tripworth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Simple layout you can create with EditTexts and Buttons (not included here)
        setContentView(R.layout.activity_main)

        // Example: Launch overlay permission settings if needed
        val btnOverlaySettings = findViewById<Button>(R.id.btn_overlay_settings)
        btnOverlaySettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(intent)
        }

        // Save preferences example (you need to add EditTexts in activity_main)
        val btnSavePrefs = findViewById<Button>(R.id.btn_save_prefs)
        btnSavePrefs.setOnClickListener {
            val hourlyRate = findViewById<EditText>(R.id.et_hourly_rate).text.toString().toFloatOrNull() ?: 30f
            val prefs = getSharedPreferences("driver_prefs", MODE_PRIVATE)
            prefs.edit().putFloat("hourly_rate", hourlyRate).apply()
        }
    }
}
