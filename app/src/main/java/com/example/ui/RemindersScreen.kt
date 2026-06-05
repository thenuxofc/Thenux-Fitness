package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun RemindersScreen(viewModel: GymViewModel) {
    val reminders by viewModel.allReminders.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var rTitle by remember { mutableStateOf("") }
    var rTimeHour by remember { mutableStateOf("07") }
    var rTimeMin by remember { mutableStateOf("00") }
    var rTimeAmPm by remember { mutableStateOf("AM") }
    var rCategory by remember { mutableStateOf("General") }

    // Selected days
    val selectedDays = remember { mutableStateListOf("Mon", "Wed", "Fri") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fitness Reminders",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Set smart reminders to hit Thenux Gym!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(FitnessBlue, CircleShape)
                        .testTag("add_reminder_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlarm,
                        contentDescription = "Add reminder",
                        tint = Color.White
                    )
                }
            }
        }

        // --- Simulated Notification Trigger Panel ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101010))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Alert", tint = FitnessOrange, modifier = Modifier.size(22.dp))
                        Text(
                            text = "Test Scheduled Reminders",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Test immediate alert triggers to verify how Thenux notifications look. Tap any below to simulate a real-time alarm alert!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateReminder(
                                    "Thenux Gym O`Clock!",
                                    "Time to crush your chest and arms workout! Get set."
                                )
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FitnessRose),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Gym Alarm 🏋️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateReminder(
                                    "Hydration Alert!",
                                    "Drink 500ml water at Thenux Fountain before cardio training."
                                )
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FitnessBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hydrate 💧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Reminders List ---
        if (reminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No Reminders active",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the top right bell button to add alarms!",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusToggle = { isEnabled ->
                        viewModel.toggleReminderStatus(reminder.id, isEnabled)
                    },
                    onDelete = {
                        viewModel.deleteReminder(reminder)
                    }
                )
            }
        }
    }

    // --- Add Reminder Modal Dialog ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Custom Reminder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Title field
                    OutlinedTextField(
                        value = rTitle,
                        onValueChange = { rTitle = it },
                        label = { Text("Alarm Name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("reminder_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = FitnessBlue
                        ),
                        singleLine = true
                    )

                    // Alarm category Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Strength", "Cardio Spark", "Hydration")
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (rCategory == cat) FitnessBlue else Color.White.copy(alpha = 0.05f))
                                    .clickable { rCategory = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rCategory == cat) Color.White else Color.LightGray
                                )
                            }
                        }
                    }

                    // Time wheel input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = rTimeHour,
                            onValueChange = { if (it.length <= 2) rTimeHour = it },
                            label = { Text("Hour", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("reminder_hour_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitnessBlue)
                        )
                        Text(":", color = Color.White, fontWeight = FontWeight.Black)
                        OutlinedTextField(
                            value = rTimeMin,
                            onValueChange = { if (it.length <= 2) rTimeMin = it },
                            label = { Text("Min", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FitnessBlue)
                        )

                        // AM / PM Box Choice
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            listOf("AM", "PM").forEach { opt ->
                                Box(
                                    modifier = Modifier
                                        .background(if (rTimeAmPm == opt) FitnessBlue else Color.Transparent)
                                        .clickable { rTimeAmPm = opt }
                                        .padding(horizontal = 8.dp, vertical = 10.dp)
                                ) {
                                    Text(text = opt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    // Days selections
                    Text(text = "Days of Week", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val wkDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        wkDays.forEach { d ->
                            val isChosen = selectedDays.contains(d)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) FitnessBlue else Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        if (isChosen) selectedDays.remove(d) else selectedDays.add(d)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = d.substring(0, 1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) Color.White else Color.LightGray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rTitle.isNotBlank()) {
                            val timeStr = "${rTimeHour.padStart(2, '0')}:${rTimeMin.padStart(2, '0')} $rTimeAmPm"
                            val dayStr = if (selectedDays.size == 7) "Daily" else selectedDays.joinToString(", ")
                            viewModel.addReminder(rTitle, timeStr, dayStr, rCategory)
                            showAddDialog = false
                            rTitle = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FitnessGreen, contentColor = Color.Black),
                    modifier = Modifier.testTag("reminder_submit_button")
                ) {
                    Text("Add Alarm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = DarkGreyWidget,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ReminderItem(
    reminder: GymReminder,
    onStatusToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .testTag("reminder_item_card_${reminder.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkGreyWidget)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Bell status circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (reminder.isEnabled) FitnessBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reminder.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Notification",
                        tint = if (reminder.isEnabled) FitnessBlue else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = reminder.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (reminder.isEnabled) Color.White else Color.Gray
                    )

                    Text(
                        text = "${reminder.timeString} • ${reminder.daysOfWeek}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (reminder.category) {
                                    "Strength" -> FitnessBlue.copy(alpha = 0.1f)
                                    "Cardio Spark" -> FitnessRose.copy(alpha = 0.1f)
                                    else -> FitnessGreen.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = reminder.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = when (reminder.category) {
                                "Strength" -> FitnessBlue
                                "Cardio Spark" -> FitnessRose
                                else -> FitnessGreen
                            }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onStatusToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FitnessBlue,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
