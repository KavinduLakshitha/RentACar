package com.example.rentacar

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize

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
) : Parcelable

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var mainScrollView: ScrollView
    private lateinit var creditBalanceText: TextView
    private lateinit var darkModeSwitch: Switch
    private lateinit var searchEditText: EditText
    private lateinit var sortButton: Button
    private lateinit var favoritesSection: LinearLayout
    private lateinit var favoritesListText: TextView
    private lateinit var carCounterText: TextView
    private lateinit var carImageView: ImageView
    private lateinit var favoriteButton: Button
    private lateinit var carNameText: TextView
    private lateinit var carModelText: TextView
    private lateinit var carYearText: TextView
    private lateinit var carRatingBar: RatingBar
    private lateinit var carRatingText: TextView
    private lateinit var carKilometresText: TextView
    private lateinit var carCostText: TextView
    private lateinit var rentButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    // All available cars (5 cars as per requirement)
    private val allCars = mutableListOf(
        Car("Toyota", "Camry", 2022, 4.5f, 25000, 45.0, "car_toyota"),
        Car("Honda", "CR-V", 2023, 4.7f, 15000, 65.0, "car_honda"),
        Car("BMW", "3 Series", 2021, 4.3f, 35000, 85.0, "car_bmw"),
        Car("Tesla", "Model 3", 2023, 4.8f, 10000, 75.0, "car_tesla"),
        Car("Ford", "Mustang", 2020, 4.2f, 45000, 55.0, "car_ford")
    )
    
    // Credit balance
    private var creditBalance = 500.0
    private val maxRentalCost = 400.0
    
    // Current state
    private var currentCarIndex = 0
    private var displayedCars = mutableListOf<Car>()
    private var isDarkMode = false
    private var currentSortOption = "None"

    companion object {
        const val REQUEST_CODE_BOOKING = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        
        initializeViews()
        updateDisplayedCars()
        displayCurrentCar()
        setupListeners()
        updateCreditBalance()
        updateFavoritesSection()
    }

    private fun initializeViews() {
        mainScrollView = findViewById(R.id.mainScrollView)
        creditBalanceText = findViewById(R.id.creditBalanceText)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        searchEditText = findViewById(R.id.searchEditText)
        sortButton = findViewById(R.id.sortButton)
        favoritesSection = findViewById(R.id.favoritesSection)
        favoritesListText = findViewById(R.id.favoritesListText)
        carCounterText = findViewById(R.id.carCounterText)
        carImageView = findViewById(R.id.carImageView)
        favoriteButton = findViewById(R.id.favoriteButton)
        carNameText = findViewById(R.id.carNameText)
        carModelText = findViewById(R.id.carModelText)
        carYearText = findViewById(R.id.carYearText)
        carRatingBar = findViewById(R.id.carRatingBar)
        carRatingText = findViewById(R.id.carRatingText)
        carKilometresText = findViewById(R.id.carKilometresText)
        carCostText = findViewById(R.id.carCostText)
        rentButton = findViewById(R.id.rentButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
    }

    private fun setupListeners() {
        // Next/Previous buttons
        nextButton.setOnClickListener {
            if (currentCarIndex < displayedCars.size - 1) {
                currentCarIndex++
                displayCurrentCar()
            }
        }

        previousButton.setOnClickListener {
            if (currentCarIndex > 0) {
                currentCarIndex--
                displayCurrentCar()
            }
        }

        // Rent button
        rentButton.setOnClickListener {
            val car = displayedCars[currentCarIndex]
            if (car.isRented) {
                Toast.makeText(this, "This car is already rented!", Toast.LENGTH_SHORT).show()
            } else {
                navigateToBooking(car)
            }
        }

        // Favorite button
        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        // Long press on image to toggle favorite
        carImageView.setOnLongClickListener {
            toggleFavorite()
            true
        }

        // Search functionality
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                filterCars()
            }
        })

        // Sort button
        sortButton.setOnClickListener {
            showSortMenu()
        }

        // Dark mode toggle
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            isDarkMode = isChecked
            applyTheme()
        }
    }

    private fun updateDisplayedCars() {
        val searchQuery = searchEditText.text.toString().lowercase().trim()
        
        // Filter: show only non-rented cars that match search
        displayedCars = allCars.filter { car ->
            !car.isRented && (searchQuery.isEmpty() || 
                car.name.lowercase().contains(searchQuery) || 
                car.model.lowercase().contains(searchQuery))
        }.toMutableList()

        // Apply sorting
        when (currentSortOption) {
            "Rating (High to Low)" -> displayedCars.sortByDescending { it.rating }
            "Year (Newest to Oldest)" -> displayedCars.sortByDescending { it.year }
            "Cost (Low to High)" -> displayedCars.sortBy { it.dailyRentalCost }
        }

        // Reset index if out of bounds
        if (currentCarIndex >= displayedCars.size) {
            currentCarIndex = if (displayedCars.isNotEmpty()) displayedCars.size - 1 else 0
        }
    }

    private fun filterCars() {
        updateDisplayedCars()
        if (displayedCars.isEmpty()) {
            Toast.makeText(this, "No cars found matching your search", Toast.LENGTH_SHORT).show()
            // Show empty state or last valid state
        } else {
            displayCurrentCar()
        }
    }

    private fun displayCurrentCar() {
        if (displayedCars.isEmpty()) {
            carNameText.text = "No cars available"
            rentButton.isEnabled = false
            return
        }

        val car = displayedCars[currentCarIndex]
        
        // Update counter
        carCounterText.text = "Car ${currentCarIndex + 1} of ${displayedCars.size}"
        
        // Update car details
        carNameText.text = car.name
        carModelText.text = car.model
        carYearText.text = car.year.toString()
        carRatingBar.rating = car.rating
        carRatingText.text = "(${car.rating})"
        carKilometresText.text = String.format("%,d km", car.kilometres)
        carCostText.text = "${car.dailyRentalCost.toInt()} credits/day"
        
        // Update favorite button
        updateFavoriteButton(car)
        
        // Update rent button
        if (car.isRented) {
            rentButton.text = "Already Rented"
            rentButton.isEnabled = false
        } else {
            rentButton.text = "Rent This Car"
            rentButton.isEnabled = true
        }
        
        // Set car image from resource
        try {
            val imageRes = resources.getIdentifier(car.imageResource, "drawable", packageName)
            if (imageRes != 0) {
                carImageView.setImageResource(imageRes)
            } else {
                // Fallback to placeholder if image not found
                carImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } catch (e: Exception) {
            // Fallback to placeholder
            carImageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        // Update navigation buttons
        previousButton.isEnabled = currentCarIndex > 0
        nextButton.isEnabled = currentCarIndex < displayedCars.size - 1
    }

    private fun toggleFavorite() {
        if (displayedCars.isEmpty()) return
        
        val car = displayedCars[currentCarIndex]
        // Find the car in allCars and toggle favorite
        val carInList = allCars.find { it.name == car.name && it.model == car.model }
        carInList?.isFavorite = !car.isFavorite
        
        // Update displayed list
        updateDisplayedCars()
        displayCurrentCar()
        updateFavoritesSection()
        
        val message = if (carInList?.isFavorite == true) "Added to favorites" else "Removed from favorites"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateFavoriteButton(car: Car) {
        if (car.isFavorite) {
            favoriteButton.text = "★ Remove from Favorites"
        } else {
            favoriteButton.text = "☆ Add to Favorites"
        }
    }

    private fun updateFavoritesSection() {
        val favorites = allCars.filter { it.isFavorite }
        
        if (favorites.isEmpty()) {
            favoritesSection.visibility = android.view.View.GONE
        } else {
            favoritesSection.visibility = android.view.View.VISIBLE
            val favoritesList = favorites.joinToString("\n") { 
                "• ${it.name} ${it.model} (${it.year})" 
            }
            favoritesListText.text = favoritesList
        }
    }

    private fun showSortMenu() {
        val options = arrayOf("None", "Rating (High to Low)", "Year (Newest to Oldest)", "Cost (Low to High)")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sort Cars By")
            .setItems(options) { _, which ->
                currentSortOption = options[which]
                updateDisplayedCars()
                currentCarIndex = 0  // Reset to first car after sorting
                displayCurrentCar()
                Toast.makeText(this, "Sorted by: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun navigateToBooking(car: Car) {
        val intent = Intent(this, BookingActivity::class.java).apply {
            putExtra("selected_car", car)
            putExtra("current_balance", creditBalance)
            putExtra("max_rental_cost", maxRentalCost)
        }
        startActivityForResult(intent, REQUEST_CODE_BOOKING)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_BOOKING && resultCode == RESULT_OK) {
            val bookingConfirmed = data?.getBooleanExtra("booking_confirmed", false) ?: false
            val bookingCancelled = data?.getBooleanExtra("booking_cancelled", false) ?: false
            val carName = data?.getStringExtra("car_name") ?: ""
            val carModel = data?.getStringExtra("car_model") ?: ""
            val totalCost = data?.getDoubleExtra("total_cost", 0.0) ?: 0.0
            
            if (bookingConfirmed) {
                // Deduct from balance
                creditBalance -= totalCost
                updateCreditBalance()
                
                // Mark car as rented
                val car = allCars.find { it.name == carName && it.model == carModel }
                car?.isRented = true
                
                // Update display
                updateDisplayedCars()
                displayCurrentCar()
                
                Toast.makeText(this, "Car rented successfully! Remaining balance: ${creditBalance.toInt()} credits", Toast.LENGTH_LONG).show()
            } else if (bookingCancelled) {
                // Restore car and balance
                val car = allCars.find { it.name == carName && it.model == carModel }
                if (car?.isRented == true) {
                    car.isRented = false
                    creditBalance += totalCost
                    updateCreditBalance()
                    updateDisplayedCars()
                    displayCurrentCar()
                    Toast.makeText(this, "Booking cancelled. Car is now available again!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateCreditBalance() {
        creditBalanceText.text = creditBalance.toInt().toString()
        
        // Change color based on balance
        val color = when {
            creditBalance >= 400 -> android.graphics.Color.parseColor("#2E7D32") // Green
            creditBalance >= 200 -> android.graphics.Color.parseColor("#F57C00") // Orange
            else -> android.graphics.Color.parseColor("#C62828") // Red
        }
        creditBalanceText.setTextColor(color)
    }

    private fun applyTheme() {
        val bgColor = if (isDarkMode) android.graphics.Color.parseColor("#121212") else android.graphics.Color.parseColor("#F5F5F5")
        val cardBgColor = if (isDarkMode) android.graphics.Color.parseColor("#1E1E1E") else android.graphics.Color.parseColor("#FFFFFF")
        val textColor = if (isDarkMode) android.graphics.Color.parseColor("#E0E0E0") else android.graphics.Color.parseColor("#333333")
        val secondaryTextColor = if (isDarkMode) android.graphics.Color.parseColor("#B0B0B0") else android.graphics.Color.parseColor("#666666")

        mainScrollView.setBackgroundColor(bgColor)
        
        // Update text colors
        carNameText.setTextColor(textColor)
        carModelText.setTextColor(textColor)
        carYearText.setTextColor(textColor)
        carKilometresText.setTextColor(textColor)
        carCounterText.setTextColor(secondaryTextColor)
        carRatingText.setTextColor(secondaryTextColor)
        
        Toast.makeText(this, if (isDarkMode) "Dark mode enabled" else "Light mode enabled", Toast.LENGTH_SHORT).show()
    }
}
