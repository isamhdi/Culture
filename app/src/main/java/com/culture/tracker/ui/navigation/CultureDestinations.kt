package com.culture.tracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomTab(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object Home : BottomTab("home", com.culture.tracker.R.string.nav_home, Icons.Filled.Home)
    data object Garden : BottomTab("garden", com.culture.tracker.R.string.nav_garden, Icons.Filled.Yard)
    data object Calendar : BottomTab("calendar", com.culture.tracker.R.string.nav_calendar, Icons.Filled.CalendarMonth)
    data object Journal : BottomTab("journal", com.culture.tracker.R.string.nav_journal, Icons.AutoMirrored.Filled.MenuBook)
    data object Settings : BottomTab("settings", com.culture.tracker.R.string.nav_settings, Icons.Filled.Settings)

    companion object {
        val all = listOf(Home, Garden, Calendar, Journal, Settings)
    }
}

object Routes {
    const val PLANT_DETAIL = "plant/{plantId}"
    const val ENVIRONMENT_DETAIL = "environment/{environmentId}"
    fun plantDetail(plantId: Long) = "plant/$plantId"
    fun environmentDetail(environmentId: Long) = "environment/$environmentId"
}
