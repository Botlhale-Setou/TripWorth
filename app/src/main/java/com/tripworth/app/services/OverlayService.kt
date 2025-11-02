package com.tripworth.app.services

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.TextView
import com.tripworth.app.R
import com.tripworth.app.models.*
import com.tripworth.app.analysis.TripAnalyzer
import android.content.Context

class OverlayService : Service() {

    private var overlayView: View? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tripData = intent?.getParcelableExtra<TripData>("trip_data")
        tripData?.let { showAnalysisOverlay(it) }
        return START_NOT_STICKY
    }

    private fun showAnalysisOverlay(tripData: TripData) {
        val prefs = getSharedPreferences("driver_prefs", MODE_PRIVATE)
        val driverPrefs = DriverPreferences(
            desiredHourlyRate = prefs.getFloat("hourly_rate", 30.0f),
            maxPickupTime = prefs.getInt("max_pickup_time", 5),
            maxPickupDistance = prefs.getFloat("max_pickup_distance", 3.0f),
            minimumFare = prefs.getFloat("minimum_fare", 5.0f)
        )

        val analyzer = TripAnalyzer(driverPrefs)
        val result = analyzer.analyzeTrip(tripData)
        createOverlayView(result)
    }

    private fun createOverlayView(result: AnalysisResult) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        removeExistingOverlay()

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_trip_analysis, null)

        val params = WindowManager.LayoutParams().apply {
            type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
            format = PixelFormat.TRANSLUCENT
        }

        configureOverlayAppearance(result)
        windowManager.addView(overlayView, params)

        Handler(Looper.getMainLooper()).postDelayed({ removeExistingOverlay() }, 8000)
    }

    private fun configureOverlayAppearance(result: AnalysisResult) {
        overlayView?.let { view ->
            val background = view.findViewById<View>(R.id.overlay_background)
            val scoreText = view.findViewById<TextView>(R.id.score_text)
            val rateText = view.findViewById<TextView>(R.id.hourly_rate_text)
            val verdictText = view.findViewById<TextView>(R.id.verdict_text)

            val (bgColor, textColor) = when (result.verdict) {
                Verdict.GOOD -> Color.GREEN to Color.WHITE
                Verdict.AVERAGE -> Color.YELLOW to Color.BLACK
                Verdict.POOR -> Color.RED to Color.WHITE
            }

            background.setBackgroundColor(bgColor)
            scoreText.setTextColor(textColor)
            rateText.setTextColor(textColor)
            verdictText.setTextColor(textColor)

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
