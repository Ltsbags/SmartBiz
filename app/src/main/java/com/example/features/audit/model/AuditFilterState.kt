package com.example.features.audit.model

data class AuditFilterState(
    val selectedModule: String? = null,
    val selectedSeverity: String? = null,
    val selectedUserId: String? = null,
    val selectedBusinessId: String? = null,
    val selectedBranchId: String? = null,
    val selectedAction: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val searchQuery: String = ""
) {
    fun hasActiveFilters(): Boolean {
        return selectedModule != null ||
                selectedSeverity != null ||
                selectedUserId != null ||
                selectedBusinessId != null ||
                selectedBranchId != null ||
                selectedAction != null ||
                startTime != null ||
                endTime != null ||
                searchQuery.isNotBlank()
    }
}
