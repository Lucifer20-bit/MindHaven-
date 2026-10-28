package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val biometricLockEnabled: Boolean = false,
    val themeMode: String = "gentle_sage",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "displayName" to displayName,
        "biometricLockEnabled" to biometricLockEnabled,
        "themeMode" to themeMode,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class CheckIn(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val moodScore: Int = 3, // 1 to 5
    val moodLabel: String = "Neutral",
    val emotionTags: List<String> = emptyList(),
    val energyLevel: Int = 3, // 1 to 5
    val sleepQuality: Int = 3, // 1 to 5
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "date" to date,
            "moodScore" to moodScore,
            "moodLabel" to moodLabel,
            "emotionTags" to emotionTags,
            "energyLevel" to energyLevel,
            "sleepQuality" to sleepQuality,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
        if (note.isNotBlank()) {
            map["note"] = note
        }
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class JournalEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val title: String = "",
    val gratitudeAnswers: List<String> = emptyList(),
    val reflection: String = "",
    val promptQuestion: String = "",
    val tags: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "userId" to userId,
            "date" to date,
            "title" to title,
            "gratitudeAnswers" to gratitudeAnswers,
            "reflection" to reflection,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
        if (promptQuestion.isNotBlank()) {
            map["promptQuestion"] = promptQuestion
        }
        if (tags.isNotEmpty()) {
            map["tags"] = tags
        }
        return map.filterValues { it != null }
    }
}

data class DailyAffirmation(
    val id: String,
    val text: String,
    val category: String,
    val author: String = "MindHaven"
)
