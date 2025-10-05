package com.example.rentacar

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for MainActivity using Espresso.
 * Focuses on text and buttons as RatingBars are not easily testable.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAppTitleIsDisplayed() {
        onView(withId(R.id.appTitleText))
            .check(matches(isDisplayed()))
            .check(matches(withText("🚗 Rent a Car")))
    }

    @Test
    fun testCreditBalanceIsDisplayed() {
        onView(withId(R.id.creditBalanceText))
            .check(matches(isDisplayed()))
            .check(matches(withText("500")))
    }

    @Test
    fun testCarDetailsAreDisplayed() {
        onView(withId(R.id.carNameText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.carModelText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.carYearText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.carKilometresText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.carCostText))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationButtons() {
        // Test Next button
        onView(withId(R.id.nextButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
        
        // Test Previous button
        onView(withId(R.id.previousButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun testSearchFunctionality() {
        onView(withId(R.id.searchEditText))
            .perform(typeText("Toyota"))
            .check(matches(withText("Toyota")))
    }

    @Test
    fun testSortButton() {
        onView(withId(R.id.sortButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun testDarkModeToggle() {
        onView(withId(R.id.darkModeSwitch))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testFavoriteButton() {
        onView(withId(R.id.favoriteButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun testRentButton() {
        onView(withId(R.id.rentButton))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
            .check(matches(withText("Rent This Car")))
    }

    @Test
    fun testCarCounterDisplay() {
        onView(withId(R.id.carCounterText))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Car"))))
    }
}
