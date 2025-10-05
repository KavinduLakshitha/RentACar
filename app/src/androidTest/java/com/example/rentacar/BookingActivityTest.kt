package com.example.rentacar

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.rentacar.model.Car
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for BookingActivity using Espresso.
 * Tests form validation and user interactions.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BookingActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule<BookingActivity>(
        Intent(ApplicationProvider.getApplicationContext(), BookingActivity::class.java).apply {
            // Create test Car object matching your actual Car data class
            val testCar = Car(
                name = "Test Car",
                model = "Model X",
                year = 2023,
                rating = 4.5f,
                kilometres = 15000,
                dailyRentalCost = 50.0,
                imageResource = "car_placeholder",
                isFavorite = false,
                isRented = false
            )

            putExtra("selected_car", testCar)
            putExtra("current_balance", 500.0)
            putExtra("max_rental_cost", 400.0)
            putExtra("is_dark_mode", false)
        }
    )

    @Test
    fun testCarDetailsAreDisplayed() {
        onView(withId(R.id.carNameText))
            .check(matches(isDisplayed()))

        onView(withId(R.id.carTypeText))
            .check(matches(isDisplayed()))

        onView(withId(R.id.carPriceText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testDaysSlider() {
        onView(withId(R.id.daysSlider))
            .check(matches(isDisplayed()))
            .perform(swipeRight())
    }

    @Test
    fun testDaysCountDisplay() {
        onView(withId(R.id.daysCountText))
            .check(matches(isDisplayed()))
            .check(matches(withText("1")))
    }

    @Test
    fun testCustomerNameInput() {
        // Scroll to the view first
        onView(withId(R.id.customerNameEdit))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(typeText("John Doe"), closeSoftKeyboard())
            .check(matches(withText("John Doe")))
    }

    @Test
    fun testEmailInput() {
        onView(withId(R.id.customerEmailEdit))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(typeText("john@example.com"), closeSoftKeyboard())
            .check(matches(withText("john@example.com")))
    }

    @Test
    fun testPhoneInput() {
        onView(withId(R.id.customerPhoneEdit))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(typeText("1234567890"), closeSoftKeyboard())
            .check(matches(withText("1234567890")))
    }

    @Test
    fun testDriverLicenseInput() {
        onView(withId(R.id.driverLicenseEdit))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(typeText("DL123456"), closeSoftKeyboard())
            .check(matches(withText("DL123456")))
    }

    @Test
    fun testAgeSpinner() {
        onView(withId(R.id.ageSpinner))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testInsuranceCheckbox() {
        // Scroll to insurance checkbox and perform click
        onView(withId(R.id.insuranceCheckBox))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testPriceSummaryDisplay() {
        // Scroll to price summary
        onView(withId(R.id.totalPriceText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSaveButton() {
        // Scroll to the save button
        onView(withId(R.id.saveButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testCancelButton() {
        // Scroll to the cancel button
        onView(withId(R.id.cancelButton))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun testBackButton() {
        // Back button is at the top, no scroll needed
        onView(withId(R.id.backButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }
}