package com.culture.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Champs Date + Heure éditables, pour dater rétroactivement un relevé (ex : "hier à 15h il faisait X°C"). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerRow(dateTime: LocalDateTime, onDateTimeChange: (LocalDateTime) -> Unit, modifier: Modifier = Modifier) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = dateTime.toLocalDate().toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            modifier = Modifier.weight(1f),
            trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Choisir") } },
        )
        OutlinedTextField(
            value = dateTime.toLocalTime().format(TimeFormatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Heure") },
            modifier = Modifier.weight(1f),
            trailingIcon = { TextButton(onClick = { showTimePicker = true }) { Text("Choisir") } },
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTime.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val newDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateTimeChange(LocalDateTime.of(newDate, dateTime.toLocalTime()))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(initialHour = dateTime.hour, initialMinute = dateTime.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateTimeChange(LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(timePickerState.hour, timePickerState.minute)))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Annuler") } },
            text = { TimePicker(state = timePickerState) },
        )
    }
}
