package com.example.features.search

import com.example.repositories.SearchResultItem
import com.example.repositories.SearchResultType

data class GlobalSearchState(
    val query: String = "",
    val results: List<SearchResultItem> = emptyList(),
    val filteredResults: List<SearchResultItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val selectedFilterType: SearchResultType? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
