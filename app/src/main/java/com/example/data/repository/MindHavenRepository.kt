package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CheckIn
import com.example.data.model.JournalEntry
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MindHavenRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.GET, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject(UserProfile::class.java))
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            val payload = if (snapshot.exists()) {
                mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName,
                    "biometricLockEnabled" to profile.biometricLockEnabled,
                    "themeMode" to profile.themeMode,
                    "createdAt" to (snapshot.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            } else {
                mapOf(
                    "userId" to uid,
                    "displayName" to profile.displayName.ifBlank { "Mindful Soul" },
                    "biometricLockEnabled" to profile.biometricLockEnabled,
                    "themeMode" to profile.themeMode,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Daily Check-Ins ---

    fun observeCheckIns(userId: String): Flow<List<CheckIn>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("checkIns")
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(CheckIn::class.java)
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveCheckIn(checkIn: CheckIn): Result<String> {
        val uid = requireUserId()
        val docId = if (checkIn.id.isNotBlank()) checkIn.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("checkIns").document(docId)

        return try {
            val existingSnap = docRef.get().await()
            val payload = if (existingSnap.exists()) {
                mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "date" to checkIn.date,
                    "moodScore" to checkIn.moodScore,
                    "moodLabel" to checkIn.moodLabel,
                    "emotionTags" to checkIn.emotionTags,
                    "energyLevel" to checkIn.energyLevel,
                    "sleepQuality" to checkIn.sleepQuality,
                    "createdAt" to existingSnap.getTimestamp("createdAt"),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).apply {
                    if (checkIn.note.isNotBlank()) put("note", checkIn.note)
                }
            } else {
                mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "date" to checkIn.date,
                    "moodScore" to checkIn.moodScore,
                    "moodLabel" to checkIn.moodLabel,
                    "emotionTags" to checkIn.emotionTags,
                    "energyLevel" to checkIn.energyLevel,
                    "sleepQuality" to checkIn.sleepQuality,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).apply {
                    if (checkIn.note.isNotBlank()) put("note", checkIn.note)
                }
            }
            docRef.set(payload.filterValues { it != null }).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCheckIn(checkInId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("checkIns").document(checkInId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Journal Entries ---

    fun observeJournalEntries(userId: String): Flow<List<JournalEntry>> = callbackFlow {
        val collectionRef = db.collection("users").document(userId).collection("journalEntries")
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.LIST, collectionRef.path)
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(JournalEntry::class.java)
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveJournalEntry(entry: JournalEntry): Result<String> {
        val uid = requireUserId()
        val docId = if (entry.id.isNotBlank()) entry.id else UUID.randomUUID().toString()
        val docRef = db.collection("users").document(uid).collection("journalEntries").document(docId)

        return try {
            val existingSnap = docRef.get().await()
            val payload = if (existingSnap.exists()) {
                mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "date" to entry.date,
                    "title" to entry.title.ifBlank { "Untitled Gratitude" },
                    "gratitudeAnswers" to entry.gratitudeAnswers,
                    "reflection" to entry.reflection,
                    "createdAt" to existingSnap.getTimestamp("createdAt"),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).apply {
                    if (entry.promptQuestion.isNotBlank()) put("promptQuestion", entry.promptQuestion)
                    if (entry.tags.isNotEmpty()) put("tags", entry.tags)
                }
            } else {
                mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "date" to entry.date,
                    "title" to entry.title.ifBlank { "Untitled Gratitude" },
                    "gratitudeAnswers" to entry.gratitudeAnswers,
                    "reflection" to entry.reflection,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).apply {
                    if (entry.promptQuestion.isNotBlank()) put("promptQuestion", entry.promptQuestion)
                    if (entry.tags.isNotEmpty()) put("tags", entry.tags)
                }
            }
            docRef.set(payload.filterValues { it != null }).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteJournalEntry(entryId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("journalEntries").document(entryId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
