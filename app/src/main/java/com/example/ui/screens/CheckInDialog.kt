package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.WellnessData
import com.example.data.model.CheckIn
import com.example.ui.theme.MoodGood
import com.example.ui.theme.MoodLow
import com.example.ui.theme.MoodNeutral
import com.example.ui.theme.MoodRadiant
import com.example.ui.theme.MoodRough

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInDialog(
    initialCheckIn: CheckIn? = null,
    onDismiss: () -> Unit,
    onSave: (moodScore: Int, moodLabel: String, emotionTags: List<String>, energyLevel: Int, sleepQuality: Int, note: String, existingId: String) -> Unit
) {
    var moodSlider by remember { mutableFloatStateOf(initialCheckIn?.moodScore?.toFloat() ?: 4f) }
    val moodInt = moodSlider.toInt().coerceIn(1, 5)

    val selectedEmotions = remember {
        mutableStateListOf<String>().apply {
            if (initialCheckIn != null) addAll(initialCheckIn.emotionTags)
            else addAll(listOf("Calm", "Grateful"))
        }
    }

    var energyLevel by remember { mutableIntStateOf(initialCheckIn?.energyLevel ?: 4) }
    var sleepQuality by remember { mutableIntStateOf(initialCheckIn?.sleepQuality ?: 4) }
    var note by remember { mutableStateOf(initialCheckIn?.note ?: "") }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomTagField by remember { mutableStateOf(false) }

    val moodInfo = getMoodDescriptor(moodInt)
    val animatedBgColor by animateColorAsState(
        targetValue = moodInfo.color.copy(alpha = 0.15f),
        animationSpec = tween(300),
        label = "moodColor"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(32.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialCheckIn != null) "Edit Today's Check-In" else "10-Second Check-In",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "How are you feeling in this moment?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_checkin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mood Selection Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = animatedBgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = moodInfo.emoji,
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = moodInfo.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = moodInfo.color
                        )
                        Text(
                            text = moodInfo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mood Slider
                        Slider(
                            value = moodSlider,
                            onValueChange = { moodSlider = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = moodInfo.color,
                                activeTrackColor = moodInfo.color,
                                inactiveTrackColor = moodInfo.color.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mood_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1 Rough", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("3 Neutral", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("5 Radiant", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Emotion Tags Section
                Text(
                    text = "What emotions are present?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val allTags = (WellnessData.defaultEmotionTags + selectedEmotions).distinct()
                    allTags.forEach { tag ->
                        val isSelected = selectedEmotions.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedEmotions.remove(tag)
                                else selectedEmotions.add(tag)
                            },
                            label = { Text(tag) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (!showCustomTagField) {
                        FilterChip(
                            selected = false,
                            onClick = { showCustomTagField = true },
                            label = { Text("+ Custom") },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                if (showCustomTagField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customTagInput,
                            onValueChange = { customTagInput = it },
                            placeholder = { Text("e.g. Centered, Creative") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (customTagInput.isNotBlank()) {
                                    val formatted = customTagInput.trim().replaceFirstChar { it.uppercase() }
                                    if (!selectedEmotions.contains(formatted)) {
                                        selectedEmotions.add(formatted)
                                    }
                                    customTagInput = ""
                                    showCustomTagField = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add tag")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Energy & Sleep Ratings
                Text(
                    text = "Body & Rest Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Energy selector (1..5)
                LevelSelectorRow(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Energy Level",
                    selectedLevel = energyLevel,
                    levelLabels = listOf("Drained", "Low", "Steady", "High", "Vibrant"),
                    onSelect = { energyLevel = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Sleep selector (1..5)
                LevelSelectorRow(
                    icon = Icons.Default.Bedtime,
                    title = "Sleep Quality",
                    selectedLevel = sleepQuality,
                    levelLabels = listOf("Restless", "Poor", "Okay", "Good", "Deep Rest"),
                    onSelect = { sleepQuality = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Optional Quick Note
                Text(
                    text = "A quick gentle note (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("What made you feel this way? (e.g. peaceful morning coffee, busy day at work)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkin_note_input"),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Save Action Button
                Button(
                    onClick = {
                        onSave(
                            moodInt,
                            moodInfo.label,
                            selectedEmotions.toList(),
                            energyLevel,
                            sleepQuality,
                            note,
                            initialCheckIn?.id ?: ""
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_checkin_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialCheckIn != null) "Update Check-In" else "Save Check-In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelSelectorRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    selectedLevel: Int,
    levelLabels: List<String>,
    onSelect: (Int) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$title: ${levelLabels.getOrElse(selectedLevel - 1) { "" }}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (1..5).forEach { level ->
                val isSelected = selectedLevel == level
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onSelect(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class MoodDescriptor(
    val score: Int,
    val label: String,
    val emoji: String,
    val description: String,
    val color: Color
)

fun getMoodDescriptor(score: Int): MoodDescriptor {
    return when (score) {
        5 -> MoodDescriptor(5, "Radiant", "☀️", "Joyful, vibrant, and flourishing", MoodRadiant)
        4 -> MoodDescriptor(4, "Good", "🌤️", "Calm, pleasant, and content", MoodGood)
        3 -> MoodDescriptor(3, "Neutral", "⛅", "Balanced, steady, and okay", MoodNeutral)
        2 -> MoodDescriptor(2, "Low", "☁️", "Tired, fragile, or heavy", MoodLow)
        else -> MoodDescriptor(1, "Rough", "🌧️", "Overwhelmed, struggling, or tender", MoodRough)
    }
}
