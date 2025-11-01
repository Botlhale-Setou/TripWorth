package com.example.tripworth.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// COPY AND PASTE THIS EXACT CODE STRUCTURE (with @Parcelize added)
@Parcelize
data class TripData(
    val fare: Float,
    val tripDistance: Float,
    val tripDuration: Int,
    val pickupDistance: Float,
    val pickupDuration: Int,
    val destination: String,
    val surgeMultiplier: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class DriverPreferences(
    val desiredHourlyRate: Float = 30.0f,
    val maxPickupTime: Int = 5,
    val maxPickupDistance: Float = 3.0f,
    val preferredZones: List<String> = emptyList(),
    val avoidedZones: List<String> = emptyList(),
    val minimumFare: Float = 5.0f,
    val includeCosts: Boolean = false,
    val fuelCostPerKm: Float = 0.15f,
    val maintenanceCostPerKm: Float = 0.05f
) : Parcelable

data class AnalysisResult(
    val score: Int,
    val netHourlyRate: Float,
    val netProfit: Float,
    val verdict: Verdict,
    val breakdown: Map<String, String>
)

enum class Verdict { GOOD, AVERAGE, POOR }
