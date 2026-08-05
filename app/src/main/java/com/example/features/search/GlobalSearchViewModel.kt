package com.example.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.utils.AppLogger
import com.example.repositories.GlobalSearchRepository
import com.example.repositories.SearchResultType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GlobalSearchViewModel(
    private val searchRepository: GlobalSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSearchState())
    val uiState: StateFlow<GlobalSearchState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentSearches()
    }

    fun loadRecentSearches() {
        viewModelScope.launch {
            val recents = searchRepository.getRecentSearches()
            _uiState.update { it.copy(recentSearches = recents) }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), filteredResults = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = searchRepository.searchAll(newQuery)
                val filter = _uiState.value.selectedFilterType
                val filtered = if (filter == null) results else results.filter { it.type == filter }
                
                _uiState.update { 
                    it.copy(
                        results = results,
                        filteredResults = filtered,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                AppLogger.e("GlobalSearchViewModel", "Search failed: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Search failed: ${e.message}") }
            }
        }
    }

    fun selectFilterType(type: SearchResultType?) {
        _uiState.update { state ->
            val newType = if (state.selectedFilterType == type) null else type
            val filtered = if (newType == null) state.results else state.results.filter { it.type == newType }
            state.copy(selectedFilterType = newType, filteredResults = filtered)
        }
    }

    fun executeRecentSearch(recentQuery: String) {
        onQueryChanged(recentQuery)
    }

    fun saveCurrentSearchToHistory() {
        val q = _uiState.value.query.trim()
        if (q.isNotEmpty()) {
            searchRepository.addRecentSearch(q)
            loadRecentSearches()
        }
    }

    fun clearSearchHistory() {
        searchRepository.clearRecentSearches()
        loadRecentSearches()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val repository: GlobalSearchRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GlobalSearchViewModel::class.java)) {
                return GlobalSearchViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
