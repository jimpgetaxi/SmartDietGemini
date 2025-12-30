package com.jimpg.smartdiet.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jimpg.smartdiet.data.local.entity.MealEntity
import com.jimpg.smartdiet.domain.repository.MealRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import com.jimpg.smartdiet.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val meals: List<MealEntity> = emptyList(),
    val dailyCalories: Int = 0,
    val calorieTarget: Int = 0,
    val userProfile: UserProfileEntity? = null
)

class HomeViewModel(private val repository: MealRepository) : ViewModel() {

    private val _meals = repository.getAllMeals()
    private val _userProfile = repository.getUserProfile()
    
    // Combine logic to create a single UI State
    val uiState: StateFlow<HomeUiState> = combine(_meals, _userProfile) { meals, profile ->
        val todayStart = getStartOfDay()
        val todayMeals = meals.filter { it.timestamp >= todayStart }
        val dailyCalories = todayMeals.sumOf { it.calories ?: 0 }
        
        HomeUiState(
            meals = meals,
            dailyCalories = dailyCalories,
            calorieTarget = profile?.calorieTarget ?: 2000, // Default fallback
            userProfile = profile
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    class Factory(private val repository: MealRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
