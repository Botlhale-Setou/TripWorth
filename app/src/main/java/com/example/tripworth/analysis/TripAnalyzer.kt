// TripAnalyzer.kt
package com.example.tripworth.analysis

import com.example.tripworth.models.*

class TripAnalyzer(private val preferences: DriverPreferences) {

    fun analyzeTrip(trip: TripData): AnalysisResult {
        // 1. Calculate total time and distance (including pickup)
        val totalTimeMinutes = trip.pickupDuration + trip.tripDuration
        val totalDistanceKm = trip.pickupDistance + trip.tripDistance

        // 2. Calculate base hourly rate (before costs)
        val baseHourlyRate = (trip.fare / totalTimeMinutes) * 60

        // 3. Calculate net profit and hourly rate (after vehicle costs)
        val totalCosts = if (preferences.includeCosts) {
            totalDistanceKm * (preferences.fuelCostPerKm + preferences.maintenanceCostPerKm)
        } else { 0f }

        val netProfit = trip.fare - totalCosts
        val netHourlyRate = (netProfit / totalTimeMinutes) * 60

        // 4. Calculate score components (0-100 scale)
        val hourlyRateScore = calculateHourlyRateScore(netHourlyRate)
        val pickupEfficiencyScore = calculatePickupEfficiencyScore(trip)
        val destinationScore = calculateDestinationScore(trip.destination)
        val fareScore = calculateFareScore(trip.fare)

        // 5. Weighted final score
        val finalScore = (
            hourlyRateScore * 0.4f +    // 40% weight to hourly rate
            pickupEfficiencyScore * 0.3f + // 30% to pickup efficiency  
            destinationScore * 0.2f +   // 20% to destination
            fareScore * 0.1f            // 10% to minimum fare
        ).toInt()

        // 6. Determine verdict
        val verdict = when {
            finalScore >= 75 -> Verdict.GOOD
            finalScore >= 50 -> Verdict.AVERAGE
            else -> Verdict.POOR
        }

        // 7. Create breakdown for UI display
        val breakdown = mapOf(
            "hourly_rate" to "$${"%.2f".format(netHourlyRate)}/h",
            "net_profit" to "$${"%.2f".format(netProfit)}",
            "pickup_time" to "${trip.pickupDuration}min",
            "total_time" to "${totalTimeMinutes}min",
            "score_breakdown" to "H:${hourlyRateScore} P:${pickupEfficiencyScore} D:${destinationScore}"
        )

        return AnalysisResult(finalScore, netHourlyRate, netProfit, verdict, breakdown)
    }

    private fun calculateHourlyRateScore(hourlyRate: Float): Int {
        val ratio = hourlyRate / preferences.desiredHourlyRate
        return (minOf(ratio, 2.0f) * 50).toInt() // Max 100 points
    }

    private fun calculatePickupEfficiencyScore(trip: TripData): Int {
        val pickupRatio = trip.pickupDuration.toFloat() / preferences.maxPickupTime
        return maxOf(0, 100 - (pickupRatio * 100).toInt())
    }

    private fun calculateDestinationScore(destination: String): Int {
        return when {
            preferences.preferredZones.any { destination.contains(it, true) } -> 100
            preferences.avoidedZones.any { destination.contains(it, true) } -> 0
            else -> 50
        }
    }

    private fun calculateFareScore(fare: Float): Int {
        return if (fare >= preferences.minimumFare) 100 else 0
    }
}
