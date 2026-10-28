package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.MindHavenRepository
import com.example.ui.MainViewModel
import com.example.ui.components.MindHavenBottomNav
import com.example.ui.components.PrivacyLockOverlay
import com.example.ui.components.ScreenTab
import com.example.ui.theme.MindHavenTheme

@Composable
fun MainFeatureScreen(
    currentUserId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val repository = remember { MindHavenRepository(context) }
    val viewModel: MainViewModel = viewModel(
        key = currentUserId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, currentUserId) as T
            }
        }
    )

    val userProfile by viewModel.userProfile.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.TODAY) }
    var journalPrefilledPrompt by remember { mutableStateOf<String?>(null) }

    // Handle back button when on sub-screens
    BackHandler(enabled = currentTab != ScreenTab.TODAY) {
        currentTab = ScreenTab.TODAY
    }

    LaunchedEffect(operationMessage) {
        operationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearOperationMessage()
        }
    }

    MindHavenTheme(themeMode = userProfile?.themeMode ?: "gentle_sage") {
        if (userProfile?.biometricLockEnabled == true && !isUnlocked) {
            PrivacyLockOverlay(
                onUnlock = { viewModel.unlockApp() }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    MindHavenBottomNav(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            if (tab != ScreenTab.JOURNAL) {
                                journalPrefilledPrompt = null
                            }
                            currentTab = tab
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        ScreenTab.TODAY -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToJournal = { prompt ->
                                journalPrefilledPrompt = prompt
                                currentTab = ScreenTab.JOURNAL
                            },
                            onNavigateToBreathing = {
                                currentTab = ScreenTab.BREATHE
                            },
                            onNavigateToMoodMap = {
                                currentTab = ScreenTab.MOOD_MAP
                            }
                        )

                        ScreenTab.JOURNAL -> GratitudeJournalScreen(
                            viewModel = viewModel,
                            prefilledPrompt = journalPrefilledPrompt
                        )

                        ScreenTab.MOOD_MAP -> MoodHeatmapScreen(
                            viewModel = viewModel,
                            onOpenCheckInForToday = {
                                currentTab = ScreenTab.TODAY
                            }
                        )

                        ScreenTab.BREATHE -> BreathingScreen()

                        ScreenTab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            coroutineScope = coroutineScope,
                            onSignOutComplete = onSignOut
                        )
                    }
                }
            }
        }
    }
}
