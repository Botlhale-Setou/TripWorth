package com.tripworth.app.analysis

import com.tripworth.app.models.*

class TripAnalyzer(private val preferences: DriverPreferences) {

    fun analyzeTrip(trip: TripData): AnalysisResult {
        val totalTimeMinutes = trip.pickupDuration + trip.tripDuration
        val totalDistanceKm = trip.pickupDistance + trip.tripDistance

        val baseHourlyRate = (trip.fare / totalTimeMinutes) * 60

        val totalCosts = if (preferences.includeCosts) {
            totalDistanceKm * (preferences.fuelCostPerKm + preferences.maintenanceCostPerKm)
        } else { 0f }

        val netProfit = trip.fare - totalCosts
        val netHourlyRate = (netProfit / totalTimeMinutes) * 60

        val hourlyRateScore = calculateHourlyRateScore(netHourlyRate)
        val pickupEfficiencyScore = calculatePickupEfficiencyScore(trip)
        val destinationScore = calculateDestinationScore(trip.destination)
        val fareScore = calculateFareScore(trip.fare)

        val finalScore = (
            hourlyRateScore * 0.4f +
            pickupEfficiencyScore * 0.3f +
            destinationScore * 0.2f +
            fareScore * 0.1f
        ).toInt()

        val verdict = when {
            finalScore >= 75 -> Verdict.GOOD
            finalScore >= 50 -> Verdict.AVERAGE
            else -> Verdict.POOR
        }

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
        return (minOf(ratio, 2.0f) * 50).toInt()
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
