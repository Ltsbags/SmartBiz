package com.example.features.commandcenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.services.ActivityAggregatorService
import com.example.services.UnifiedActivityItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ActivityFilterState(
    val query: String = "",
    val sourceFilter: String? = null, // AUDIT_LOG, NOTIFICATION, SECURITY_EVENT, TRANSACTION
    val categoryFilter: String? = null
)

class ActivityCenterViewModel(
    private val activityAggregatorService: ActivityAggregatorService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sourceFilter = MutableStateFlow<String?>(null)
    val sourceFilter: StateFlow<String?> = _sourceFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val filterState = combine(_searchQuery, _sourceFilter, _categoryFilter) { query, source, category ->
        ActivityFilterState(query, source, category)
    }

    val activities: StateFlow<List<UnifiedActivityItem>> = combine(
        activityAggregatorService.unifiedActivityFeed,
        filterState
    ) { feed, filter ->
        feed.filter { item ->
            val matchesQuery = filter.query.isEmpty() ||
                    item.title.contains(filter.query, ignoreCase = true) ||
                    item.description.contains(filter.query, ignoreCase = true) ||
                    item.actor.contains(filter.query, ignoreCase = true)

            val matchesSource = filter.sourceFilter == null || item.source.equals(filter.sourceFilter, ignoreCase = true)
            val matchesCategory = filter.categoryFilter == null || item.category.equals(filter.categoryFilter, ignoreCase = true)

            matchesQuery && matchesSource && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSourceFilter(source: String?) {
        _sourceFilter.value = source
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    class Factory(
        private val activityAggregatorService: ActivityAggregatorService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityCenterViewModel(activityAggregatorService) as T
        }
    }
}
