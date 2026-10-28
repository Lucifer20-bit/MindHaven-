package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CheckIn
import com.example.data.model.JournalEntry
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class MindHavenRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveCheckIn_authenticatedUser_successAndEmitsViaFlow() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MindHavenRepository(firestore)

        val checkIn = CheckIn(
            userId = aliceUid,
            date = "2026-10-28",
            moodScore = 4,
            moodLabel = "Good",
            emotionTags = listOf("Calm", "Grateful"),
            energyLevel = 4,
            sleepQuality = 5,
            note = "Morning sun and quiet breeze."
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveCheckIn(checkIn) }
        assertTrue("Expected saveCheckIn to succeed: ${result.exceptionOrNull()}", result.isSuccess)
        val docId = result.getOrThrow()
        assertTrue(docId.isNotEmpty())

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCheckIns(aliceUid).first { list -> list.any { it.id == docId } }
        }
        val found = emittedList.first { it.id == docId }
        assertEquals(4, found.moodScore)
        assertEquals("Good", found.moodLabel)
    }

    @Test
    fun saveJournalEntry_authenticatedUser_successAndReadsBack() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = MindHavenRepository(firestore)

        val entry = JournalEntry(
            userId = aliceUid,
            date = "2026-10-28",
            title = "Gentle Afternoon Reflection",
            gratitudeAnswers = listOf("Warm tea", "Restful sleep", "Friendly message"),
            reflection = "Today I learned to let go of small worries.",
            promptQuestion = "What brought you comfort today?",
            tags = listOf("Peace", "Gratitude")
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveJournalEntry(entry) }
        assertTrue("Expected saveJournalEntry to succeed: ${result.exceptionOrNull()}", result.isSuccess)
        val docId = result.getOrThrow()

        val emittedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeJournalEntries(aliceUid).first { list -> list.any { it.id == docId } }
        }
        val found = emittedList.first { it.id == docId }
        assertEquals("Gentle Afternoon Reflection", found.title)
        assertEquals(3, found.gratitudeAnswers.size)
    }

    @Test
    fun unauthenticatedUser_observeCheckIns_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = MindHavenRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCheckIns("arbitrary_user_id").first()
            }
            fail("Expected unauthenticated query to throw exception")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_wellness@test.com"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
