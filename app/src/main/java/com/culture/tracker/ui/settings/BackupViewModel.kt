package com.culture.tracker.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.backup.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isWorking: Boolean = false,
    val message: String? = null,
    val restoreSucceeded: Boolean = false,
)

class BackupViewModel(private val repository: BackupRepository) : ViewModel() {
    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState(isWorking = true)
            val result = repository.export(uri)
            _state.value = BackupUiState(
                message = if (result.isSuccess) {
                    "Sauvegarde exportée."
                } else {
                    "Échec de l'export : ${result.exceptionOrNull()?.message}"
                },
            )
        }
    }

    fun restoreFrom(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupUiState(isWorking = true)
            val result = repository.import(uri)
            _state.value = if (result.isSuccess) {
                BackupUiState(restoreSucceeded = true)
            } else {
                BackupUiState(message = "Échec de la restauration : ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
