package com.tripworth.app.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import com.tripworth.app.models.TripData

class RideCaptureService : AccessibilityService() {

    private val targetPackages = listOf("com.ubercab", "com.taxify", "com.bolt.client") // include common package names

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            val pkg = it.packageName?.toString()
            if (pkg != null && targetPackages.any { t -> pkg.contains(t, true) }) {
                val screenText = it.text.joinToString(" ")
                analyzeScreenText(screenText)
            }
        }
    }

    private fun analyzeScreenText(text: String) {
        val farePattern = """\$\d+\.?\d*""".toRegex()
        val distancePattern = """(\d+\.?\d*)\s*km""".toRegex()
        val timePattern = """(\d+)\s*min""".toRegex()

        val fare = farePattern.find(text)?.value?.removePrefix("$")?.toFloatOrNull() ?: 0f
        val distances = distancePattern.findAll(text).mapNotNull { it.groupValues.getOrNull(1)?.toFloatOrNull() }.toList()
        val times = timePattern.findAll(text).mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }.toList()

        if (fare > 0 && distances.size >= 2 && times.size >= 2) {
            val tripData = TripData(
                fare = fare,
                pickupDistance = distances[0],
                tripDistance = distances[1],
                pickupDuration = times[0],
                tripDuration = times[1],
                destination = extractDestination(text)
            )

            val intent = Intent(this, OverlayService::class.java).apply {
                putExtra("trip_data", tripData)
            }
            startService(intent)
        }
    }

    private fun extractDestination(text: String): String {
        val patterns = listOf(
            """to\s+([A-Za-z\s]+)""",
            """destination:\s*([A-Za-z\s]+)"""
        )
        patterns.forEach { pattern ->
            pattern.toRegex(RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.let {
                return it.trim()
            }
        }
        return "Unknown"
    }

    override fun onInterrupt() {}
}
