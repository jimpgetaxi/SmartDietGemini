package com.jimpg.smartdiet.presentation.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jimpg.smartdiet.data.ai.GeminiService
import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.data.model.MealAnalysisResponse
import com.jimpg.smartdiet.domain.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddMealUiState(
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val analysisResult: MealAnalysisResponse? = null,
    val isSaved: Boolean = false
)

class AddMealViewModel(
    private val repository: MealRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealUiState())
    val uiState: StateFlow<AddMealUiState> = _uiState.asStateFlow()

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun analyzeMeal() {
        val description = _uiState.value.description
        if (description.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null, analysisResult = null) }

        viewModelScope.launch {
            // Fetch User Profile & History
            val userProfile = repository.getUserProfileSync()
            val history = repository.getLast20Meals()
            
            val result = geminiService.analyzeMeal(description, userProfile, history)
            result.fold(
                onSuccess = { analysis ->
                    _uiState.update { it.copy(isLoading = false, analysisResult = analysis) }
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
            )
        }
    }

    fun saveMeal() {
        val analysis = _uiState.value.analysisResult ?: return
        
        viewModelScope.launch {
            val meal = MealEntity(
                description = analysis.description,
                timestamp = System.currentTimeMillis(),
                calories = analysis.calories,
                protein = analysis.protein,
                carbs = analysis.carbs,
                fat = analysis.fat,
                rawAnalysis = analysis.analysis
            )
            repository.insertMeal(meal)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun resetState() {
        _uiState.value = AddMealUiState()
    }

    class Factory(
        private val repository: MealRepository,
        private val geminiService: GeminiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddMealViewModel::class.java)) {
                return AddMealViewModel(repository, geminiService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
