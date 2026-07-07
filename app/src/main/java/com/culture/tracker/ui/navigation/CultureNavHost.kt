package com.culture.tracker.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.culture.tracker.ui.calendar.CalendarScreen
import com.culture.tracker.ui.garden.GardenScreen
import com.culture.tracker.ui.garden.environments.EnvironmentDetailScreen
import com.culture.tracker.ui.garden.plants.PlantDetailScreen
import com.culture.tracker.ui.genetics.GeneticsScreen
import com.culture.tracker.ui.home.HomeScreen
import com.culture.tracker.ui.journal.JournalScreen
import com.culture.tracker.ui.settings.SettingsScreen
import com.culture.tracker.ui.tools.ToolsScreen

@Composable
fun CultureNavHost() {
    val navController = rememberNavController()

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            FloatingBottomNav(
                isSelected = { tab -> currentRoute?.hierarchy?.any { it.route == tab.route } == true },
                onTabSelected = { tab -> navigateToTab(tab.route) },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    onPlantClick = { plantId -> navController.navigate(Routes.plantDetail(plantId)) },
                    onNavigateToCalendar = { navigateToTab(BottomTab.Calendar.route) },
                    onNavigateToGarden = { navigateToTab(BottomTab.Garden.route) },
                )
            }
            composable(BottomTab.Garden.route) {
                GardenScreen(
                    onPlantClick = { plantId -> navController.navigate(Routes.plantDetail(plantId)) },
                    onEnvironmentClick = { environmentId -> navController.navigate(Routes.environmentDetail(environmentId)) },
                )
            }
            composable(BottomTab.Calendar.route) { CalendarScreen() }
            composable(BottomTab.Journal.route) { JournalScreen() }
            composable(BottomTab.Settings.route) {
                SettingsScreen(
                    onOpenTools = { navController.navigate("tools") },
                    onOpenGenetics = { navController.navigate("genetics") },
                )
            }
            composable("tools") { ToolsScreen(onBack = { navController.popBackStack() }) }
            composable("genetics") { GeneticsScreen(onBack = { navController.popBackStack() }) }

            composable(
                route = Routes.PLANT_DETAIL,
                arguments = listOf(navArgument("plantId") { type = androidx.navigation.NavType.LongType }),
            ) { backStackEntry ->
                val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
                PlantDetailScreen(plantId = plantId, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.ENVIRONMENT_DETAIL,
                arguments = listOf(navArgument("environmentId") { type = androidx.navigation.NavType.LongType }),
            ) { backStackEntry ->
                val environmentId = backStackEntry.arguments?.getLong("environmentId") ?: return@composable
                EnvironmentDetailScreen(environmentId = environmentId, onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Barre de navigation flottante en pilule : l'onglet actif s'étend pour révéler son libellé. */
@Composable
private fun FloatingBottomNav(
    isSelected: (BottomTab) -> Boolean,
    onTabSelected: (BottomTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomTab.all.forEach { tab ->
                val selected = isSelected(tab)
                val backgroundColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    label = "navPillColor",
                )
                val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = if (selected) 16.dp else 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(tab.icon, contentDescription = stringResource(tab.labelRes), tint = contentColor)
                    if (selected) {
                        Text(stringResource(tab.labelRes), color = contentColor, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
