package com.example.rentacar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rentacar.model.Car
import androidx.core.graphics.toColorInt

class BookingActivity : AppCompatActivity() {

    private lateinit var selectedCar: Car
    private lateinit var mainScrollView: ScrollView
    private lateinit var carNameText: TextView
    private lateinit var carTypeText: TextView
    private lateinit var carPriceText: TextView
    private lateinit var carFeaturesText: TextView
    private lateinit var carImageView: ImageView

    private lateinit var daysSlider: SeekBar
    private lateinit var daysCountText: TextView
    private lateinit var customerNameEdit: EditText
    private lateinit var customerEmailEdit: EditText
    private lateinit var customerPhoneEdit: EditText
    private lateinit var driverLicenseEdit: EditText
    private lateinit var ageSpinner: Spinner
    private lateinit var insuranceCheckBox: CheckBox
    private lateinit var totalPriceText: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: TextView

    private var rentalDays = 1
    private var currentBalance = 500.0
    private var maxRentalCost = 400.0
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        supportActionBar?.hide()

        // Get the car data and balance from intent (using modern API)
        selectedCar = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("selected_car", Car::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("selected_car")!!
        }
        currentBalance = intent.getDoubleExtra("current_balance", 500.0)
        maxRentalCost = intent.getDoubleExtra("max_rental_cost", 400.0)
        isDarkMode = intent.getBooleanExtra("is_dark_mode", false)

        initializeViews()
        setupCarDetails()
        setupDaysSlider()
        setupAgeSpinner()
        setupPriceCalculation()
        setupButtons()
        setupBackPressHandler()

        // Initial price calculation
        calculateTotalPrice()
        
        // Apply dark mode if enabled
        if (isDarkMode) {
            applyDarkMode()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showCancelConfirmation()
            }
        })
    }

    private fun initializeViews() {
        mainScrollView = findViewById(R.id.mainScrollView)
        carNameText = findViewById(R.id.carNameText)
        carTypeText = findViewById(R.id.carTypeText)
        carPriceText = findViewById(R.id.carPriceText)
        carFeaturesText = findViewById(R.id.carFeaturesText)
        carImageView = findViewById(R.id.carImageView)

        daysSlider = findViewById(R.id.daysSlider)
        daysCountText = findViewById(R.id.daysCountText)
        customerNameEdit = findViewById(R.id.customerNameEdit)
        customerEmailEdit = findViewById(R.id.customerEmailEdit)
        customerPhoneEdit = findViewById(R.id.customerPhoneEdit)
        driverLicenseEdit = findViewById(R.id.driverLicenseEdit)
        ageSpinner = findViewById(R.id.ageSpinner)
        insuranceCheckBox = findViewById(R.id.insuranceCheckBox)
        totalPriceText = findViewById(R.id.totalPriceText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        backButton = findViewById(R.id.backButton)
    }

    @SuppressLint("SetTextI18n", "DiscouragedApi")
    private fun setupCarDetails() {
        carNameText.text = "${selectedCar.name} ${selectedCar.model}"
        carTypeText.text = selectedCar.year.toString()
        carPriceText.text = "${selectedCar.dailyRentalCost.toInt()} credits/day"

        // Display car features based on rating and year
        val features = mutableListOf<String>()
        if (selectedCar.rating >= 4.5f) features.add("High Rating")
        if (selectedCar.year >= 2022) features.add("New Model")
        if (selectedCar.kilometres < 20000) features.add("Low Mileage")
        features.add("${selectedCar.kilometres / 1000}k km")

        carFeaturesText.text = features.joinToString(" • ")

        // Set car image from resource
        try {
            val imageRes = resources.getIdentifier(selectedCar.imageResource, "drawable", packageName)
            if (imageRes != 0) {
                carImageView.setImageResource(imageRes)
            } else {
                // Fallback to placeholder if image not found
                carImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } catch (_: Exception) {
            // Fallback to placeholder
            carImageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupDaysSlider() {
        daysSlider.max = 6  // 0-6 means 1-7 days
        daysSlider.progress = 0  // Start at 1 day
        daysCountText.text = "1"
        rentalDays = 1

        daysSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rentalDays = progress + 1  // 0-6 becomes 1-7
                daysCountText.text = rentalDays.toString()
                calculateTotalPrice()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupAgeSpinner() {
        val ages = (18..80).map { "$it years" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ageSpinner.adapter = adapter
        ageSpinner.setSelection(7) // Default to 25 years
    }

    private fun setupPriceCalculation() {
        insuranceCheckBox.setOnCheckedChangeListener { _, _ ->
            calculateTotalPrice()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun calculateTotalPrice() {
        val baseCost = selectedCar.dailyRentalCost * rentalDays
        val insuranceCost = if (insuranceCheckBox.isChecked) baseCost * 0.15 else 0.0
        val tax = (baseCost + insuranceCost) * 0.1
        val totalCost = baseCost + insuranceCost + tax

        val priceBreakdown = buildString {
            append("Price Breakdown:\n")
            append("Base cost ($rentalDays day(s)): ${String.format("%.0f", baseCost)} credits\n")
            if (insuranceCheckBox.isChecked) {
                append("Insurance (15%): ${String.format("%.0f", insuranceCost)} credits\n")
            }
            append("Tax (10%): ${String.format("%.0f", tax)} credits\n")
            append("━━━━━━━━━━━━━━━━━━━\n")
            append("Total: ${String.format("%.0f", totalCost)} credits\n")
            append("\nYour Balance: ${currentBalance.toInt()} credits")

            // Show warning if over limit
            if (totalCost > maxRentalCost) {
                append("\n\nWARNING: Total exceeds max rental limit of ${maxRentalCost.toInt()} credits!")
            } else if (totalCost > currentBalance) {
                append("\n\nWARNING: Insufficient balance!")
            }
        }

        totalPriceText.text = priceBreakdown

        // Change total price text color if there's an issue
        // Use appropriate colors based on dark mode
        val normalTextColor = if (isDarkMode) {
            "#E0E0E0".toColorInt() // Light for dark mode
        } else {
            "#333333".toColorInt() // Dark for light mode
        }
        
        val textColor = when {
            totalCost > maxRentalCost || totalCost > currentBalance ->
                "#C62828".toColorInt() // Red for errors (same in both modes)
            else -> normalTextColor
        }
        totalPriceText.setTextColor(textColor)
    }

    private fun setupButtons() {
        saveButton.setOnClickListener {
            if (validateInputs() && validateCreditBalance()) {
                showBookingConfirmation()
            }
        }

        cancelButton.setOnClickListener {
            showCancelConfirmation()
        }

        backButton.setOnClickListener {
            // Back button acts as cancel
            showCancelConfirmation()
        }
    }

    private fun validateInputs(): Boolean {
        val name = customerNameEdit.text.toString().trim()
        val email = customerEmailEdit.text.toString().trim()
        val phone = customerPhoneEdit.text.toString().trim()
        val license = driverLicenseEdit.text.toString().trim()

        when {
            name.isEmpty() -> {
                customerNameEdit.error = "Name is required"
                customerNameEdit.requestFocus()
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                customerEmailEdit.error = "Valid email is required"
                customerEmailEdit.requestFocus()
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return false
            }
            phone.isEmpty() || phone.length < 10 -> {
                customerPhoneEdit.error = "Valid phone number is required (min 10 digits)"
                customerPhoneEdit.requestFocus()
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                return false
            }
            license.isEmpty() -> {
                driverLicenseEdit.error = "Driver license number is required"
                driverLicenseEdit.requestFocus()
                Toast.makeText(this, "Driver license number is required", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    @SuppressLint("DefaultLocale")
    private fun validateCreditBalance(): Boolean {
        val baseCost = selectedCar.dailyRentalCost * rentalDays
        val insuranceCost = if (insuranceCheckBox.isChecked) baseCost * 0.15 else 0.0
        val tax = (baseCost + insuranceCost) * 0.1
        val totalCost = baseCost + insuranceCost + tax

        when {
            totalCost > maxRentalCost -> {
                AlertDialog.Builder(this)
                    .setTitle("Rental Limit Exceeded")
                    .setMessage("The total cost (${String.format("%.0f", totalCost)} credits) exceeds the maximum rental limit of ${maxRentalCost.toInt()} credits per booking.\n\nPlease reduce the number of days or remove insurance.")
                    .setPositiveButton("OK", null)
                    .show()
                return false
            }
            totalCost > currentBalance -> {
                AlertDialog.Builder(this)
                    .setTitle("Insufficient Balance")
                    .setMessage("Your current balance (${currentBalance.toInt()} credits) is insufficient.\n\nTotal cost: ${String.format("%.0f", totalCost)} credits\nShortfall: ${String.format("%.0f", totalCost - currentBalance)} credits\n\nPlease reduce the rental duration or remove insurance.")
                    .setPositiveButton("OK", null)
                    .show()
                return false
            }
        }
        return true
    }

    @SuppressLint("DefaultLocale")
    private fun showBookingConfirmation() {
        val baseCost = selectedCar.dailyRentalCost * rentalDays
        val insuranceCost = if (insuranceCheckBox.isChecked) baseCost * 0.15 else 0.0
        val tax = (baseCost + insuranceCost) * 0.1
        val totalCost = baseCost + insuranceCost + tax

        val confirmationMessage = buildString {
            append("Booking Confirmation\n\n")
            append("Car: ${selectedCar.name} ${selectedCar.model} (${selectedCar.year})\n")
            append("Customer: ${customerNameEdit.text}\n")
            append("Email: ${customerEmailEdit.text}\n")
            append("Phone: ${customerPhoneEdit.text}\n")
            append("Rental Duration: $rentalDays day(s)\n")
            append("Daily Rate: ${selectedCar.dailyRentalCost.toInt()} credits\n")
            if (insuranceCheckBox.isChecked) {
                append("Insurance: Yes\n")
            }
            append("\nTotal Cost: ${String.format("%.0f", totalCost)} credits\n")
            append("Remaining Balance: ${String.format("%.0f", currentBalance - totalCost)} credits\n\n")
            append("Confirm this booking?")
        }

        AlertDialog.Builder(this)
            .setTitle("Confirm Booking")
            .setMessage(confirmationMessage)
            .setPositiveButton("Confirm") { _, _ ->
                completeBooking(totalCost)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking? All entered information will be lost.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelBooking()
            }
            .setNegativeButton("No, Continue", null)
            .show()
    }

    @SuppressLint("DefaultLocale")
    private fun completeBooking(totalCost: Double) {
        val resultIntent = Intent().apply {
            putExtra("booking_confirmed", true)
            putExtra("car_name", selectedCar.name)
            putExtra("car_model", selectedCar.model)
            putExtra("customer_name", customerNameEdit.text.toString())
            putExtra("rental_days", rentalDays)
            putExtra("total_cost", totalCost)
        }

        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun cancelBooking() {
        val resultIntent = Intent().apply {
            putExtra("booking_confirmed", false)
            putExtra("booking_cancelled", true)
        }

        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun applyDarkMode() {
        val bgColor = "#121212".toColorInt()
        val cardBgColor = "#1E1E1E".toColorInt()
        val textColor = "#E0E0E0".toColorInt()
        val secondaryTextColor = "#B0B0B0".toColorInt()
        val inputBgColor = "#2C2C2C".toColorInt()
        val hintColor = "#888888".toColorInt()

        // Apply to main scroll view
        mainScrollView.setBackgroundColor(bgColor)

        // Find and update all card containers
        val carDetailsCard = findViewById<LinearLayout>(R.id.carDetailsCard)
        val rentalDurationCard = findViewById<LinearLayout>(R.id.rentalDurationCard)
        val customerInfoCard = findViewById<LinearLayout>(R.id.customerInfoCard)
        val additionalOptionsCard = findViewById<LinearLayout>(R.id.additionalOptionsCard)
        val priceSummaryCard = findViewById<LinearLayout>(R.id.priceSummaryCard)

        carDetailsCard?.setBackgroundColor(cardBgColor)
        rentalDurationCard?.setBackgroundColor(cardBgColor)
        customerInfoCard?.setBackgroundColor(cardBgColor)
        additionalOptionsCard?.setBackgroundColor(cardBgColor)
        priceSummaryCard?.setBackgroundColor(cardBgColor)

        // Update main text colors
        carNameText.setTextColor(textColor)
        carTypeText.setTextColor(textColor)
        carFeaturesText.setTextColor(secondaryTextColor)
        totalPriceText.setTextColor(textColor)

        // Update section header colors
        val sectionHeaders = listOf(
            findViewById<TextView>(R.id.carDetailsHeader),
            findViewById<TextView>(R.id.rentalDurationHeader),
            findViewById<TextView>(R.id.customerInfoHeader),
            findViewById<TextView>(R.id.additionalOptionsHeader),
            findViewById<TextView>(R.id.priceSummaryHeader)
        )
        sectionHeaders.forEach { it?.setTextColor(textColor) }

        // Update input fields - background and text color
        customerNameEdit.setBackgroundColor(inputBgColor)
        customerNameEdit.setTextColor(textColor)
        customerNameEdit.setHintTextColor(hintColor)

        // Fix TextInputLayout hint color for Full Name field
        val nameInputLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.nameInputLayout)
        nameInputLayout?.setHintTextColor(android.content.res.ColorStateList.valueOf(hintColor))
        nameInputLayout?.defaultHintTextColor = android.content.res.ColorStateList.valueOf(hintColor)

        customerEmailEdit.setBackgroundColor(inputBgColor)
        customerEmailEdit.setTextColor(textColor)
        customerEmailEdit.setHintTextColor(hintColor)

        customerPhoneEdit.setBackgroundColor(inputBgColor)
        customerPhoneEdit.setTextColor(textColor)
        customerPhoneEdit.setHintTextColor(hintColor)

        driverLicenseEdit.setBackgroundColor(inputBgColor)
        driverLicenseEdit.setTextColor(textColor)
        driverLicenseEdit.setHintTextColor(hintColor)

        // Update labels
        val ageLabel = findViewById<TextView>(R.id.ageLabel)
        ageLabel?.setTextColor(textColor)

        val daysLabel = findViewById<TextView>(R.id.daysLabel)
        daysLabel?.setTextColor(textColor)

        daysCountText.setTextColor(textColor)

        // Update Age Spinner
        ageSpinner.setBackgroundColor(inputBgColor)
        // Force the spinner popup to use dark colors by recreating the adapter
        val currentSelection = ageSpinner.selectedItemPosition
        val ages = (18..80).map { "$it years" }
        val darkAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ages) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(textColor)
                return view
            }
            
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(textColor)
                view.setBackgroundColor(inputBgColor)
                return view
            }
        }
        darkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        ageSpinner.adapter = darkAdapter
        ageSpinner.setSelection(currentSelection)

        // Update checkbox text
        insuranceCheckBox.setTextColor(textColor)

        // Update slider description
        val sliderDescription = findViewById<TextView>(R.id.sliderDescription)
        sliderDescription?.setTextColor(secondaryTextColor)

        // Update header
        val bookingHeaderLayout = findViewById<LinearLayout>(R.id.bookingHeaderLayout)
        bookingHeaderLayout?.setBackgroundColor(cardBgColor)
        
        val bookingHeaderTitle = findViewById<TextView>(R.id.bookingHeaderTitle)
        bookingHeaderTitle?.setTextColor(textColor)
        
        // Update button text colors
        saveButton.setTextColor("#FFFFFF".toColorInt())
        cancelButton.setTextColor(textColor)
        backButton.setTextColor(textColor)

        // Update price summary background (remove light green background)
        totalPriceText.setBackgroundColor("#252525".toColorInt())
    }
}