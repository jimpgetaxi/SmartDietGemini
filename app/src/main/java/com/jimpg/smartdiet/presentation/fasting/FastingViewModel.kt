package com.jimpg.smartdiet.presentation.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jimpg.smartdiet.data.local.dao.FastingDao
import com.jimpg.smartdiet.data.local.entity.FastingSessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class FastingStage(
    val startHour: Int,
    val endHour: Int,
    val title: String,
    val description: String,
    val iconEmoji: String // Simple way to add visual flair
)

data class FastingUiState(
    val isFasting: Boolean = false,
    val startTime: Long = 0,
    val elapsedTimeStr: String = "00:00:00",
    val elapsedHours: Float = 0f,
    val progress: Float = 0f, // 0 to 1 based on target
    val targetHours: Int = 16,
    val currentStage: FastingStage? = null,
    val nextStage: FastingStage? = null
)

class FastingViewModel(private val fastingDao: FastingDao) : ViewModel() {

    private val _uiState = MutableStateFlow(FastingUiState())
    val uiState: StateFlow<FastingUiState> = _uiState.asStateFlow()

    private val stages = listOf(
        FastingStage(0, 4, "Αύξηση Σακχάρου", "Το σώμα σου χωνεύει το τελευταίο γεύμα. Τα επίπεδα ινσουλίνης είναι υψηλά.", "😋"),
        FastingStage(4, 8, "Πτώση Σακχάρου", "Η ινσουλίνη αρχίζει να πέφτει. Το σώμα ετοιμάζεται για καύση λίπους.", "📉"),
        FastingStage(8, 12, "Επαναφορά", "Το στομάχι έχει αδειάσει. Η έκκριση αυξητικής ορμόνης ξεκινά.", "😌"),
        FastingStage(12, 18, "Κέτωση (Ήπια)", "Το σώμα αρχίζει να καίει λίπος για ενέργεια αντί για γλυκόζη.", "🔥"),
        FastingStage(18, 24, "Αυτοφαγία (Έναρξη)", "Κυτταρικός καθαρισμός. Το σώμα ανακυκλώνει παλιά κύτταρα.", "♻️"),
        FastingStage(24, 48, "Αυτοφαγία (Κορύφωση)", "Μέγιστη κυτταρική ανανέωση και αύξηση αυξητικής ορμόνης.", "🚀"),
        FastingStage(48, 72, "Ανοσοποιητική Αναγέννηση", "Βαθιά ανανέωση του ανοσοποιητικού συστήματος.", "🛡️"),
        FastingStage(72, 1000, "Παρατεταμένη Νηστεία", "Προσοχή: Συμβουλευτείτε γιατρό για νηστείες άνω των 72 ωρών.", "⚠️")
    )

    init {
        loadCurrentFast()
        startTimer()
    }

    private fun loadCurrentFast() {
        viewModelScope.launch {
            fastingDao.getCurrentFast().collect { session ->
                if (session != null && session.endTime == null) {
                    // Active fast
                    _uiState.update { 
                        it.copy(
                            isFasting = true, 
                            startTime = session.startTime,
                            targetHours = session.targetDurationHours
                        ) 
                    }
                    updateMetrics()
                } else {
                    // No active fast
                    _uiState.update { it.copy(isFasting = false) }
                }
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                if (_uiState.value.isFasting) {
                    updateMetrics()
                }
                delay(1000) // Update every second
            }
        }
    }

    private fun updateMetrics() {
        val start = _uiState.value.startTime
        val now = System.currentTimeMillis()
        val diff = now - start
        
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
        
        val elapsedHoursFloat = diff / (1000f * 60 * 60)
        
        // Find Stage
        val currentStage = stages.find { elapsedHoursFloat >= it.startHour && elapsedHoursFloat < it.endHour }
        val nextStage = stages.find { it.startHour > elapsedHoursFloat }

        // Calculate Progress relative to CURRENT STAGE (Progress towards next milestone)
        val progress = if (currentStage != null) {
            val stageDuration = currentStage.endHour - currentStage.startHour
            val timeInStage = elapsedHoursFloat - currentStage.startHour
            (timeInStage / stageDuration).coerceIn(0f, 1f)
        } else {
            0f
        }

        _uiState.update {
            it.copy(
                elapsedTimeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                elapsedHours = elapsedHoursFloat,
                currentStage = currentStage,
                nextStage = nextStage,
                progress = progress
            )
        }
    }

    fun toggleFasting() {
        viewModelScope.launch {
            val current = fastingDao.getCurrentFastSync()
            
            if (current != null && current.endTime == null) {
                // STOP Fasting
                val finishedSession = current.copy(endTime = System.currentTimeMillis())
                fastingDao.updateFast(finishedSession)
                _uiState.update { it.copy(isFasting = false) }
            } else {
                // START Fasting (Open ended - just track start time)
                val newSession = FastingSessionEntity(
                    startTime = System.currentTimeMillis(),
                    targetDurationHours = 0 // 0 means open-ended/unspecified
                )
                fastingDao.startFast(newSession)
                _uiState.update { 
                    it.copy(
                        isFasting = true, 
                        startTime = newSession.startTime,
                        targetHours = 0
                    ) 
                }
            }
        }
    }

    class Factory(private val fastingDao: FastingDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FastingViewModel::class.java)) {
                return FastingViewModel(fastingDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
