package com.culture.tracker.ui.garden

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.culture.tracker.R
import com.culture.tracker.ui.garden.environments.EnvironmentsScreen
import com.culture.tracker.ui.garden.plants.PlantsScreen

@Composable
fun GardenScreen(onPlantClick: (Long) -> Unit = {}, onEnvironmentClick: (Long) -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(R.string.garden_tab_plants, R.string.garden_tab_environments)

    Column {
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
