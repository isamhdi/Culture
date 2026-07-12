package com.culture.tracker.ui.garden

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.culture.tracker.R
import com.culture.tracker.ui.garden.environments.EnvironmentsScreen
import com.culture.tracker.ui.garden.plants.PlantsScreen

@Composable
fun GardenScreen(initialTab: Int = 0, onPlantClick: (Long) -> Unit = {}, onEnvironmentClick: (Long) -> Unit = {}) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf(R.string.garden_tab_plants, R.string.garden_tab_environments)

    // Pas de TopAppBar sur cet écran : le PrimaryTabRow est le tout premier élément visible,
    // il doit donc réserver lui-même l'inset de la barre de statut.
    Column(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, labelRes ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(stringResource(labelRes)) },
                )
            }
        }
        when (selectedTab) {
            0 -> PlantsScreen(onPlantClick = onPlantClick)
            1 -> EnvironmentsScreen(onEnvironmentClick = onEnvironmentClick)
        }
    }
}
