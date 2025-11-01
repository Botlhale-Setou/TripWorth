// OverlayService.kt
package com.example.tripworth.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.TextView
import com.example.tripworth.R
import com.example.tripworth.models.*
import com.example.tripworth.analysis.TripAnalyzer
import android.content.Context

class OverlayService : Service() {

    private var overlayView: View? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If using Parcelable TripData pass directly; otherwise parse JSON string
        val tripData = intent?.getParcelableExtra<TripData>("trip_data")
        tripData?.let {
            showAnalysisOverlay(it)
        }
        return START_NOT_STICKY
    }

    private fun showAnalysisOverlay(tripData: TripData) {
        // Get driver preferences from SharedPreferences
        val prefs = getSharedPreferences("driver_prefs", MODE_PRIVATE)
        val driverPrefs = DriverPreferences(
            desiredHourlyRate = prefs.getFloat("hourly_rate", 30.0f),
            maxPickupTime = prefs.getInt("max_pickup_time", 5),
            maxPickupDistance = prefs.getFloat("max_pickup_distance", 3.0f),
            minimumFare = prefs.getFloat("minimum_fare", 5.0f)
        )

        // Analyze the trip
        val analyzer = TripAnalyzer(driverPrefs)
        val result = analyzer.analyzeTrip(tripData)

        // Create overlay view
        createOverlayView(result)
    }

    private fun createOverlayView(result: AnalysisResult) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        removeExistingOverlay()

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_trip_analysis, null)

        // Set up view parameters
        val params = WindowManager.LayoutParams().apply {
            // TYPE_APPLICATION_OVERLAY requires SYSTEM_ALERT_WINDOW permission and API >= 26 handling
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                   WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
            format = PixelFormat.TRANSLUCENT
        }

        // Configure view based on analysis result
        configureOverlayAppearance(result)

        windowManager.addView(overlayView, params)

        // Auto-dismiss after 8 seconds (typical decision window)
        Handler(Looper.getMainLooper()).postDelayed({
            removeExistingOverlay()
        }, 8000)
    }

    private fun configureOverlayAppearance(result: AnalysisResult) {
        overlayView?.let { view ->
            val background = view.findViewById<View>(R.id.overlay_background)
            val scoreText = view.findViewById<TextView>(R.id.score_text)
            val rateText = view.findViewById<TextView>(R.id.hourly_rate_text)
            val verdictText = view.findViewById<TextView>(R.id.verdict_text)

            // Set colors based on verdict
            val (bgColor, textColor) = when (result.verdict) {
                Verdict.GOOD -> Color.GREEN to Color.WHITE
                Verdict.AVERAGE -> Color.YELLOW to Color.BLACK
                Verdict.POOR -> Color.RED to Color.WHITE
            }

            background.setBackgroundColor(bgColor)
            scoreText.setTextColor(textColor)
            rateText.setTextColor(textColor)
            verdictText.setTextColor(textColor)

            // Set text content
            scoreText.text = "Score: ${result.score}/100"
            rateText.text = "Rate: $${"%.2f".format(result.netHourlyRate)}/h"
            verdictText.text = result.verdict.name
        }
    }

    private fun removeExistingOverlay() {
        overlayView?.let {
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
            overlayView = null
        }
    }
}
