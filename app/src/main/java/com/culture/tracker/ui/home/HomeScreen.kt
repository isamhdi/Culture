package com.culture.tracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.culture.tracker.R
import com.culture.tracker.ui.components.CircularProgressRing
import com.culture.tracker.ui.components.MonthCalendar
import com.culture.tracker.ui.components.PlantCard
import com.culture.tracker.ui.components.StatTile
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel(), onPlantClick: (Long) -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_home)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1C5CAB), Color(0xFF1BAF7A))))
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressRing(
                            progress = state.todayCompletionRatio,
                            size = 76.dp,
                            strokeWidth = 8.dp,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            gradientColors = listOf(Color.White, Color.White.copy(alpha = 0.6f)),
                        ) {
                            Text(
                                "${(state.todayCompletionRatio * 100).toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Column {
                            Text("Aujourd'hui", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${state.todayDoneCount}/${state.todayScheduledCount} tâches complétées",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        label = "Plantes actives",
                        value = state.plants.size.toString(),
                        icon = Icons.Filled.Spa,
                        modifier = Modifier.weight(1f),
                        contentColor = Color.White,
                        containerBrush = Brush.linearGradient(listOf(Color(0xFF3B6939), Color(0xFF1BAF7A))),
                    )
                    StatTile(
                        label = "Alertes",
                        value = state.riskFactors.size.toString(),
                        icon = Icons.Filled.Warning,
                        modifier = Modifier.weight(1f),
                        contentColor = Color.White,
                        containerBrush = Brush.linearGradient(listOf(Color(0xFFE34948), Color(0xFFEB6834))),
                    )
                }
            }

            if (state.plants.isNotEmpty()) {
                item { Text("Vos plantes", style = MaterialTheme.typography.titleMedium) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.plants, key = { it.id }) { plant ->
                            val envName = state.environments.firstOrNull { it.id == plant.environmentId }?.name
                            PlantCard(
                                plant = plant,
                                thumbnailPath = state.thumbnails[plant.id],
                                environmentName = envName,
                                onClick = { onPlantClick(plant.id) },
                                modifier = Modifier.width(160.dp),
                            )
                        }
                    }
                }
            }

            item {
                Card {
                    MonthCalendar(
                        yearMonth = state.visibleMonth,
                        selectedDate = state.selectedDate,
                        dotsForDate = { date ->
                            state.actionsInMonth.filter { it.date == date }
                                .map { com.culture.tracker.ui.components.DotSpec(androidx.compose.ui.graphics.Color(it.actionType.colorHex)) }
                        },
                        onDateClick = viewModel::onDateSelected,
                        onPreviousMonth = viewModel::onPreviousMonth,
                        onNextMonth = viewModel::onNextMonth,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(state.moonPhase.emoji, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text("Phase lunaire du jour", style = MaterialTheme.typography.labelMedium)
                            Text(state.moonPhase.label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item {
                Text("Facteurs pouvant altérer la croissance", style = MaterialTheme.typography.titleMedium)
            }

            if (state.riskFactors.isEmpty()) {
                item {
                    Text(
                        "Aucune alerte pour le moment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.riskFactors) { risk ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Column {
                                Text(risk.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(risk.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}
