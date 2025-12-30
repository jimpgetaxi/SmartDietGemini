package com.jimpg.smartdiet.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jimpg.smartdiet.data.local.entity.UserProfileEntity
import com.jimpg.smartdiet.domain.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val nickname: String = "",
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val gender: String = "Male", // Default
    val activityLevel: Float = 1.2f, // Default Sedentary
    val bmi: Float = 0f,
    val calorieTarget: Int = 0,
    val selectedConditions: Set<String> = emptySet(),
    val customConditionInput: String = "",
    val isSaved: Boolean = false
)

class ProfileViewModel(private val repository: MealRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val availableConditions = listOf(
        "Διαβήτης",
        "Υψηλή Χοληστερίνη",
        "Υπέρταση",
        "Κοιλιοκάκη",
        "Δυσανεξία στη Λακτόζη",
        "Vegan",
        "Vegetarian"
    )
    
    val activityLevels = mapOf(
        "Καθιστική ζωή (Γραφείο, λίγη άσκηση)" to 1.2f,
        "Ελαφρά ενεργός (Άσκηση 1-3 φορές/εβδ.)" to 1.375f,
        "Μέτρια ενεργός (Άσκηση 3-5 φορές/εβδ.)" to 1.55f,
        "Πολύ ενεργός (Άσκηση 6-7 φορές/εβδ.)" to 1.725f
    )

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfileSync()
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        nickname = profile.nickname,
                        weight = profile.weight.toString(),
                        height = profile.height.toString(),
                        age = profile.age.toString(),
                        gender = profile.gender,
                        activityLevel = profile.activityLevel,
                        bmi = profile.bmi,
                        calorieTarget = profile.calorieTarget,
                        selectedConditions = if (profile.healthConditions.isNotEmpty())
                            profile.healthConditions.split(",").toSet()
                        else emptySet()
                    )
                }
            }
        }
    }

    fun onNicknameChange(newValue: String) {
        _uiState.update { it.copy(nickname = newValue) }
    }

    fun onWeightChange(newValue: String) {
        _uiState.update { it.copy(weight = newValue) }
        calculateMetrics()
    }

    fun onHeightChange(newValue: String) {
        _uiState.update { it.copy(height = newValue) }
        calculateMetrics()
    }

    fun onAgeChange(newValue: String) {
        _uiState.update { it.copy(age = newValue) }
        calculateMetrics()
    }
    
    fun onGenderChange(newValue: String) {
        _uiState.update { it.copy(gender = newValue) }
        calculateMetrics()
    }
    
    fun onActivityLevelChange(newValue: Float) {
        _uiState.update { it.copy(activityLevel = newValue) }
        calculateMetrics()
    }

    fun onCustomConditionInputChange(newValue: String) {
        _uiState.update { it.copy(customConditionInput = newValue) }
    }

    fun addCustomCondition() {
        val input = _uiState.value.customConditionInput.trim()
        if (input.isNotBlank()) {
            _uiState.update { currentState ->
                val currentConditions = currentState.selectedConditions.toMutableSet()
                currentConditions.add(input)
                currentState.copy(
                    selectedConditions = currentConditions,
                    customConditionInput = "" 
                )
            }
        }
    }

    fun removeCondition(condition: String) {
        _uiState.update { currentState ->
            val currentConditions = currentState.selectedConditions.toMutableSet()
            currentConditions.remove(condition)
            currentState.copy(selectedConditions = currentConditions)
        }
    }

    fun toggleCondition(condition: String) {
        _uiState.update { currentState ->
            val currentConditions = currentState.selectedConditions.toMutableSet()
            if (currentConditions.contains(condition)) {
                currentConditions.remove(condition)
            } else {
                currentConditions.add(condition)
            }
            currentState.copy(selectedConditions = currentConditions)
        }
    }

    private fun calculateMetrics() {
        val weight = _uiState.value.weight.toFloatOrNull()
        val height = _uiState.value.height.toFloatOrNull()
        val age = _uiState.value.age.toIntOrNull()
        val gender = _uiState.value.gender
        val activity = _uiState.value.activityLevel

        if (weight != null && height != null && height > 0 && age != null) {
            // BMI Calculation
            val heightInMeters = height / 100
            val bmi = weight / (heightInMeters * heightInMeters)
            
            // BMR Calculation (Mifflin-St Jeor)
            var bmr = (10 * weight) + (6.25 * height) - (5 * age)
            bmr += if (gender == "Male") 5 else -161
            
            // TDEE (Total Daily Energy Expenditure)
            val tdee = bmr * activity
            
            // Target for Weight Loss (Deficit ~500 kcal, but not below safe limits)
            val target = (tdee - 500).toInt().coerceAtLeast(1200)

            _uiState.update { it.copy(bmi = bmi, calorieTarget = target) }
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val weight = currentState.weight.toFloatOrNull() ?: 0f
        val height = currentState.height.toFloatOrNull() ?: 0f
        val age = currentState.age.toIntOrNull() ?: 0

        if (currentState.nickname.isNotBlank() && weight > 0 && height > 0 && age > 0) {
            viewModelScope.launch {
                val profile = UserProfileEntity(
                    nickname = currentState.nickname,
                    weight = weight,
                    height = height,
                    age = age,
                    gender = currentState.gender,
                    activityLevel = currentState.activityLevel,
                    bmi = currentState.bmi,
                    calorieTarget = currentState.calorieTarget,
                    healthConditions = currentState.selectedConditions.joinToString(",")
                )
                repository.saveUserProfile(profile)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }

    class Factory(private val repository: MealRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}