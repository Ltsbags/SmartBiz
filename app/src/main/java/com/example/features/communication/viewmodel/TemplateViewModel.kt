package com.example.features.communication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.CommunicationTemplateEntity
import com.example.repositories.TemplateRepository
import com.example.services.communication.TemplateEngineService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TemplateUiState(
    val templates: List<CommunicationTemplateEntity> = emptyList(),
    val previewSubject: String = "",
    val previewBody: String = "",
    val missingVariables: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val messageNotice: String? = null
)

class TemplateViewModel(
    private val templateRepository: TemplateRepository,
    private val templateEngineService: TemplateEngineService
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.activeTemplates.collect { list ->
                _uiState.value = _uiState.value.copy(templates = list)
            }
        }
    }

    fun saveTemplate(template: CommunicationTemplateEntity) {
        viewModelScope.launch {
            templateRepository.saveTemplate(template)
            _uiState.value = _uiState.value.copy(messageNotice = "Template '${template.name}' saved successfully")
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            templateRepository.deleteTemplate(id)
            _uiState.value = _uiState.value.copy(messageNotice = "Template deleted")
        }
    }

    fun generatePreview(template: CommunicationTemplateEntity, sampleVars: Map<String, String>) {
        val rendered = templateEngineService.renderTemplate(template, sampleVars)
        _uiState.value = _uiState.value.copy(
            previewSubject = rendered.subject,
            previewBody = rendered.body,
            missingVariables = rendered.missingVariables
        )
    }

    fun clearNotice() {
        _uiState.value = _uiState.value.copy(messageNotice = null)
    }

    class Factory(
        private val templateRepository: TemplateRepository,
        private val templateEngineService: TemplateEngineService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TemplateViewModel(templateRepository, templateEngineService) as T
        }
    }
}
