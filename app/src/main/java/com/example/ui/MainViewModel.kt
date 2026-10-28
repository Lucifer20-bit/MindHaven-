package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WellnessData
import com.example.data.model.CheckIn
import com.example.data.model.DailyAffirmation
import com.example.data.model.JournalEntry
import com.example.data.model.UserProfile
import com.example.data.repository.MindHavenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(
    private val repository: MindHavenRepository,
    val currentUserId: String
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { emit(null) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = null
        )

    val checkIns: StateFlow<List<CheckIn>> = repository.observeCheckIns(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val journalEntries: StateFlow<List<JournalEntry>> = repository.observeJournalEntries(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _currentAffirmation = MutableStateFlow(WellnessData.getAffirmationForToday())
    val currentAffirmation: StateFlow<DailyAffirmation> = _currentAffirmation.asStateFlow()

    private val _currentGratitudePromptIndex = MutableStateFlow(0)
    val currentGratitudePromptIndex: StateFlow<Int> = _currentGratitudePromptIndex.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        // Initialize user profile if not exists
        viewModelScope.launch {
            repository.saveUserProfile(
                UserProfile(
                    userId = currentUserId,
                    displayName = "Mindful Soul",
                    biometricLockEnabled = false,
                    themeMode = "gentle_sage"
                )
            )
        }
    }

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        if (userProfile.value?.biometricLockEnabled == true) {
            _isUnlocked.value = false
        }
    }

    fun shuffleAffirmation() {
        val next = WellnessData.affirmations.random()
        _currentAffirmation.value = next
    }

    fun shuffleGratitudePrompt() {
        _currentGratitudePromptIndex.value = (_currentGratitudePromptIndex.value + 1) % WellnessData.gratitudePrompts.size
    }

    fun submitCheckIn(
        moodScore: Int,
        moodLabel: String,
        emotionTags: List<String>,
        energyLevel: Int,
        sleepQuality: Int,
        note: String,
        existingId: String = ""
    ) {
        viewModelScope.launch {
            val checkIn = CheckIn(
                id = existingId,
                userId = currentUserId,
                date = WellnessData.getTodayDateString(),
                moodScore = moodScore,
                moodLabel = moodLabel,
                emotionTags = emotionTags,
                energyLevel = energyLevel,
                sleepQuality = sleepQuality,
                note = note.trim()
            )
            val result = repository.saveCheckIn(checkIn)
            if (result.isSuccess) {
                _operationMessage.value = "Check-in logged gently for today"
            } else {
                _operationMessage.value = "Failed to sync check-in"
            }
        }
    }

    fun saveJournal(
        title: String,
        gratitudeAnswers: List<String>,
        reflection: String,
        promptQuestion: String,
        tags: List<String>,
        entryId: String = ""
    ) {
        viewModelScope.launch {
            val entry = JournalEntry(
                id = entryId,
                userId = currentUserId,
                date = WellnessData.getTodayDateString(),
                title = title.ifBlank { "Daily Gratitude" },
                gratitudeAnswers = gratitudeAnswers.filter { it.isNotBlank() },
                reflection = reflection.trim(),
                promptQuestion = promptQuestion,
                tags = tags
            )
            val result = repository.saveJournalEntry(entry)
            if (result.isSuccess) {
                _operationMessage.value = "Gratitude saved to your private journal"
            } else {
                _operationMessage.value = "Could not save journal entry"
            }
        }
    }

    fun deleteJournal(entryId: String) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entryId)
            _operationMessage.value = "Entry removed"
        }
    }

    fun deleteCheckIn(checkInId: String) {
        viewModelScope.launch {
            repository.deleteCheckIn(checkInId)
            _operationMessage.value = "Check-in removed"
        }
    }

    fun updateTheme(themeMode: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile(userId = currentUserId)
            repository.saveUserProfile(current.copy(themeMode = themeMode))
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile(userId = currentUserId)
            repository.saveUserProfile(current.copy(biometricLockEnabled = enabled))
        }
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    fun calculateStreak(checkInsList: List<CheckIn>): Int {
        if (checkInsList.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val loggedDates = checkInsList.map { it.date }.toSet()

        val cal = Calendar.getInstance()
        var streak = 0

        // Check if today is logged, or if yesterday was logged to maintain streak
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        var checkCal = Calendar.getInstance()
        if (loggedDates.contains(todayStr)) {
            // Count backwards from today
        } else if (loggedDates.contains(yesterdayStr)) {
            // Checked in yesterday, today is pending
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            return 0
        }

        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (loggedDates.contains(dateStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}
