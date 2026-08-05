package com.example.features.usermanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.LoginHistoryEntity
import com.example.repositories.LoginHistoryRepository
import com.example.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginHistoryUiState(
    val historyList: List<LoginHistoryEntity> = emptyList(),
    val filterAction: String = "ALL", // ALL, SUCCESSFUL_LOGIN, FAILED_LOGIN, PIN_CHANGED, LOGOUT
    val isLoading: Boolean = false,
    val message: String? = null
)

class LoginHistoryViewModel(
    private val historyRepository: LoginHistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginHistoryUiState())
    val uiState: StateFlow<LoginHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = userRepository.getPrimaryUser()
            if (user != null) {
                historyRepository.getHistoryForUser(user.userId).collect { logs ->
                    _uiState.update { state ->
                        state.copy(
                            historyList = logs,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filterAction = filter) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            val user = userRepository.getPrimaryUser() ?: return@launch
            historyRepository.clearHistory(user.userId)
            _uiState.update { it.copy(message = "Login history cleared") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    class Factory(
        private val historyRepository: LoginHistoryRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginHistoryViewModel(historyRepository, userRepository) as T
        }
    }
}
