package com.example.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.NotificationEntity
import com.example.repositories.NotificationRepository
import com.example.services.NotificationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotificationTab {
    ALL, UNREAD, PINNED, ARCHIVED
}

data class NotificationFilterState(
    val tab: NotificationTab = NotificationTab.ALL,
    val query: String = "",
    val category: String? = null,
    val priority: String? = null
)

class NotificationCenterViewModel(
    private val notificationRepository: NotificationRepository,
    private val notificationEngine: NotificationEngine
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(NotificationTab.ALL)
    val selectedTab: StateFlow<NotificationTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _priorityFilter = MutableStateFlow<String?>(null)
    val priorityFilter: StateFlow<String?> = _priorityFilter.asStateFlow()

    val unreadCount: StateFlow<Int> = notificationRepository.unreadCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val filterState = combine(
        _selectedTab,
        _searchQuery,
        _categoryFilter,
        _priorityFilter
    ) { tab, query, category, priority ->
        NotificationFilterState(tab, query, category, priority)
    }

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        notificationRepository.allNotificationsFlow,
        notificationRepository.archivedNotificationsFlow,
        filterState
    ) { allNotifs, archivedNotifs, filter ->
        val baseList = when (filter.tab) {
            NotificationTab.ALL -> allNotifs.filter { !it.isArchived }
            NotificationTab.UNREAD -> allNotifs.filter { it.status == "UNREAD" && !it.isArchived }
            NotificationTab.PINNED -> allNotifs.filter { it.isPinned && !it.isArchived }
            NotificationTab.ARCHIVED -> archivedNotifs
        }

        baseList.filter { notif ->
            val matchesQuery = filter.query.isEmpty() ||
                    notif.title.contains(filter.query, ignoreCase = true) ||
                    notif.message.contains(filter.query, ignoreCase = true) ||
                    notif.type.contains(filter.query, ignoreCase = true)

            val matchesCat = filter.category == null || notif.type.equals(filter.category, ignoreCase = true)
            val matchesPrio = filter.priority == null || notif.priority.equals(filter.priority, ignoreCase = true)

            matchesQuery && matchesCat && matchesPrio
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: NotificationTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun setPriorityFilter(priority: String?) {
        _priorityFilter.value = priority
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun togglePin(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            notificationRepository.togglePin(id, !isPinned)
        }
    }

    fun archiveNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.archiveNotification(id)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    fun clearArchived() {
        viewModelScope.launch {
            notificationRepository.clearArchived()
        }
    }

    class Factory(
        private val notificationRepository: NotificationRepository,
        private val notificationEngine: NotificationEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationCenterViewModel(notificationRepository, notificationEngine) as T
        }
    }
}
