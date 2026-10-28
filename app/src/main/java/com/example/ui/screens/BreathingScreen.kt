package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class BreathingPattern(
    val title: String,
    val subtitle: String,
    val inhaleSeconds: Int,
    val holdAfterInhale: Int,
    val exhaleSeconds: Int,
    val holdAfterExhale: Int
) {
    BOX_4_4(
        title = "Box (4-4-4-4)",
        subtitle = "Restores mental clarity & nervous system balance",
        inhaleSeconds = 4,
        holdAfterInhale = 4,
        exhaleSeconds = 4,
        holdAfterExhale = 4
    ),
    RELAX_4_7_8(
        title = "4-7-8 Relax",
        subtitle = "Deeply calms anxiety & prepares for rest",
        inhaleSeconds = 4,
        holdAfterInhale = 7,
        exhaleSeconds = 8,
        holdAfterExhale = 0
    ),
    CALM_4_4(
        title = "Calm (4-4)",
        subtitle = "Simple, steady rhythm for everyday tranquility",
        inhaleSeconds = 4,
        holdAfterInhale = 0,
        exhaleSeconds = 4,
        holdAfterExhale = 0
    )
}

enum class BreathPhase(val instruction: String, val prompt: String) {
    INHALE("Inhale", "Breathe in deeply through your nose..."),
    HOLD_IN("Hold", "Gently rest in this fullness..."),
    EXHALE("Exhale", "Slowly let go through your mouth..."),
    HOLD_OUT("Rest", "Embrace the quiet pause...")
}

@Composable
fun BreathingScreen() {
    var selectedPattern by remember { mutableStateOf(BreathingPattern.BOX_4_4) }
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsRemainingInPhase by remember { mutableIntStateOf(selectedPattern.inhaleSeconds) }
    var completedCycles by remember { mutableIntStateOf(0) }

    val circleScale = remember { Animatable(0.55f) }

    // Breathing loop
    LaunchedEffect(isRunning, selectedPattern) {
        if (!isRunning) {
            circleScale.snapTo(0.55f)
            currentPhase = BreathPhase.INHALE
            secondsRemainingInPhase = selectedPattern.inhaleSeconds
            return@LaunchedEffect
        }

        while (isRunning) {
            // INHALE
            currentPhase = BreathPhase.INHALE
            secondsRemainingInPhase = selectedPattern.inhaleSeconds
            circleScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(selectedPattern.inhaleSeconds * 1000, easing = LinearEasing)
            )

            // HOLD AFTER INHALE
            if (selectedPattern.holdAfterInhale > 0 && isRunning) {
                currentPhase = BreathPhase.HOLD_IN
                secondsRemainingInPhase = selectedPattern.holdAfterInhale
                delay((selectedPattern.holdAfterInhale * 1000).toLong())
            }

            // EXHALE
            if (isRunning) {
                currentPhase = BreathPhase.EXHALE
                secondsRemainingInPhase = selectedPattern.exhaleSeconds
                circleScale.animateTo(
                    targetValue = 0.55f,
                    animationSpec = tween(selectedPattern.exhaleSeconds * 1000, easing = LinearEasing)
                )
            }

            // HOLD AFTER EXHALE
            if (selectedPattern.holdAfterExhale > 0 && isRunning) {
                currentPhase = BreathPhase.HOLD_OUT
                secondsRemainingInPhase = selectedPattern.holdAfterExhale
                delay((selectedPattern.holdAfterExhale * 1000).toLong())
            }

            if (isRunning) {
                completedCycles++
            }
        }
    }

    // Countdown timer ticks inside phase
    LaunchedEffect(isRunning, currentPhase, secondsRemainingInPhase) {
        if (isRunning && secondsRemainingInPhase > 1) {
            delay(1000)
            secondsRemainingInPhase--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
            .testTag("breathing_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Title & Pattern Selector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Mindful Breathing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tune in to the rhythm of your breath",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pattern Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BreathingPattern.entries.forEach { pattern ->
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = {
                            if (!isRunning) {
                                selectedPattern = pattern
                                secondsRemainingInPhase = pattern.inhaleSeconds
                            }
                        },
                        label = { Text(pattern.title) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Center Section: Breathing Lotus Orb
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val primaryContainer = MaterialTheme.colorScheme.primaryContainer

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2) * circleScale.value

                // Outer gentle glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryContainer.copy(alpha = 0.5f),
                            primaryColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    radius = radius * 1.15f,
                    center = center
                )

                // Main expanding breathing sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryContainer,
                            primaryColor.copy(alpha = 0.85f)
                        )
                    ),
                    radius = radius,
                    center = center
                )

                // Inner peaceful stroke ring
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = radius * 0.7f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isRunning) currentPhase.instruction else "Ready",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                if (isRunning) {
                    Text(
                        text = "$secondsRemainingInPhase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Phase guidance caption
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRunning) currentPhase.prompt else selectedPattern.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (completedCycles > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cycles completed: $completedCycles",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRunning) {
                Button(
                    onClick = { isRunning = false },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("pause_breathing_button")
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Button(
                    onClick = { isRunning = true },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(54.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("start_breathing_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (completedCycles > 0) "Resume Breath" else "Begin Breathing", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (completedCycles > 0 || isRunning) {
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        completedCycles = 0
                        currentPhase = BreathPhase.INHALE
                        secondsRemainingInPhase = selectedPattern.inhaleSeconds
                    },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }
        }
    }
}
