package com.example.rentacar.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data model representing a car in the rental system.
 * Contains all required car information and rental state.
 * 
 * This model is stored in memory only (no persistent storage).
 * Data is lost when the app is closed.
 */
@Parcelize
data class Car(
    val name: String,
    val model: String,
    val year: Int,
    val rating: Float,  // 1-5 stars
    val kilometres: Int,
    val dailyRentalCost: Double,
    val imageResource: String = "",
    var isFavorite: Boolean = false,
    var isRented: Boolean = false
) : Parcelable {
    
    /**
     * Get formatted display name (e.g., "Toyota Camry")
     */
    fun getDisplayName(): String = "$name $model"
    
    /**
     * Get formatted cost string (e.g., "45 credits/day")
     */
    fun getFormattedCost(): String = "${dailyRentalCost.toInt()} credits/day"
    
    /**
     * Get formatted kilometres string (e.g., "25,000 km")
     */
    fun getFormattedKilometres(): String = String.format("%,d km", kilometres)
    
    /**
     * Check if car is available for rental
     */
    fun isAvailable(): Boolean = !isRented
    
    /**
     * Get car features based on rating and year
     */
    fun getFeatures(): List<String> {
        val features = mutableListOf<String>()
        if (rating >= 4.5f) features.add("High Rating")
        if (year >= 2022) features.add("New Model")
        if (kilometres < 20000) features.add("Low Mileage")
        features.add("${kilometres / 1000}k km")
        return features
    }
}
