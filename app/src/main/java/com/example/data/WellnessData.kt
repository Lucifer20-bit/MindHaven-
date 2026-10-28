package com.example.data

import com.example.data.model.DailyAffirmation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WellnessData {

    val affirmations = listOf(
        DailyAffirmation(
            id = "aff_1",
            text = "I inhale peace and release what I cannot control.",
            category = "Peace"
        ),
        DailyAffirmation(
            id = "aff_2",
            text = "There is joy in the simple, quiet moments of today.",
            category = "Gratitude"
        ),
        DailyAffirmation(
            id = "aff_3",
            text = "I give myself permission to rest, heal, and recharge.",
            category = "Self-Compassion"
        ),
        DailyAffirmation(
            id = "aff_4",
            text = "My feelings are valid, and I hold space for myself with gentleness.",
            category = "Mindfulness"
        ),
        DailyAffirmation(
            id = "aff_5",
            text = "Small steps taken with kindness lead to profound growth.",
            category = "Inner Strength"
        ),
        DailyAffirmation(
            id = "aff_6",
            text = "I am worthy of peace, love, and contentment right here, right now.",
            category = "Self-Compassion"
        ),
        DailyAffirmation(
            id = "aff_7",
            text = "Today, I choose to notice the beauty around me.",
            category = "Gratitude"
        ),
        DailyAffirmation(
            id = "aff_8",
            text = "Like the ocean waves, challenging feelings rise and gently pass.",
            category = "Peace"
        ),
        DailyAffirmation(
            id = "aff_9",
            text = "I honor my body's need for slow rhythm and deep breath.",
            category = "Mindfulness"
        ),
        DailyAffirmation(
            id = "aff_10",
            text = "I have survived every hard day so far, and I trust my resilience.",
            category = "Inner Strength"
        )
    )

    val gratitudePrompts = listOf(
        "What is a small, unexpected thing that brought you comfort today?",
        "Who is someone whose presence in your life you appreciate, and why?",
        "What is a sound, scent, or sight from nature that brought you peace?",
        "What is something your body did for you today that you are thankful for?",
        "What was a delicious meal, drink, or warm moment you savored recently?",
        "What is a challenge that taught you something valuable about yourself?",
        "What piece of art, music, or literature lifted your spirits lately?",
        "What is a simple modern convenience you are grateful to have today?",
        "What is a memory from this past week that still makes you smile?",
        "What kindness did someone offer you, or did you offer someone else?"
    )

    val defaultEmotionTags = listOf(
        "Calm", "Grateful", "Joyful", "Inspired", "Peaceful",
        "Loved", "Hopeful", "Grounded", "Tired", "Anxious",
        "Stressed", "Overwhelmed", "Sad", "Restless", "Reflective"
    )

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getFormattedDisplayDate(dateStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr) ?: return dateStr
            val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun getAffirmationForToday(): DailyAffirmation {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % affirmations.size
        return affirmations[index]
    }
}
