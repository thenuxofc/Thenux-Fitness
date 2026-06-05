package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarScreen(viewModel: GymViewModel) {
    val allGymDays by viewModel.allGymDays.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()

    // Keep track of the currently viewed YearMonth
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val selectedLocalDate = try {
        LocalDate.parse(selectedDateStr, formatter)
    } catch (e: Exception) {
        LocalDate.now()
    }

    var currentYearMonth by remember { mutableStateOf(YearMonth.of(selectedLocalDate.year, selectedLocalDate.month)) }

    // Selected day journal states
    val isGymDayOnSelected = allGymDays.find { it.dateString == selectedDateStr }
    var noteText by remember(selectedDateStr, isGymDayOnSelected) {
        mutableStateOf(isGymDayOnSelected?.notes ?: "")
    }
    var workoutType by remember(selectedDateStr, isGymDayOnSelected) {
        mutableStateOf(isGymDayOnSelected?.workoutType ?: "General")
    }
    var ratingValue by remember(selectedDateStr, isGymDayOnSelected) {
        mutableStateOf((isGymDayOnSelected?.rating ?: 5).toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Custom Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gym Calendar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            // Current Month Label and Nav buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Prev month", tint = Color.White)
                }

                Text(
                    text = "${currentYearMonth.month.getDisplayName(TextStyle.SHORT, Locale.US)} ${currentYearMonth.year}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = FitnessRose
                )

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- Calendar Grid ---
        iOSCalendarGrid(
            yearMonth = currentYearMonth,
            selectedDate = selectedLocalDate,
            gymDays = allGymDays,
            onDayClick = { day ->
                val clickedDate = LocalDate.of(currentYearMonth.year, currentYearMonth.month, day)
                viewModel.setSelectedDate(clickedDate)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Selected Date Journal Panel ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(8.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkGreyWidget)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header displaying Selected Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = FitnessRose,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = selectedLocalDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isGymDayOnSelected != null) FitnessGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isGymDayOnSelected != null) "Gym Visited" else "Rest Day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGymDayOnSelected != null) FitnessGreen else Color.LightGray
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f))

                // Toggle gym attendance trigger
                Button(
                    onClick = {
                        viewModel.toggleGymAttendance(selectedLocalDate, ratingValue.toInt(), workoutType)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("toggle_gym_day_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGymDayOnSelected != null) Color(0xFFC62828) else FitnessRose,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isGymDayOnSelected != null) Icons.Default.Cancel else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGymDayOnSelected != null) "Mark as Rest Day" else "Mark as Gym Day!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Conditional fields for Gym diary (Only if marked)
                AnimatedVisibility(
                    visible = isGymDayOnSelected != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Type field
                        Text(text = "Workout Category", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf("Strength", "Legs & Core", "Cardio Spark", "General")
                            categories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (workoutType == cat) FitnessRose else Color.White.copy(alpha = 0.05f))
                                        .clickable { workoutType = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (workoutType == cat) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        // Star rating energy (Slider)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Workout Energy Rating", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Stars", tint = FitnessOrange, modifier = Modifier.size(16.dp))
                                Text(text = "${ratingValue.toInt()} / 5", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                        Slider(
                            value = ratingValue,
                            onValueChange = { ratingValue = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = FitnessRose,
                                activeTrackColor = FitnessRose,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        // Journal note field
                        Text(text = "Workout Notes & Logs", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("e.g. Completed chest press, 15m cardio sprint. Feeling high energy!", fontSize = 13.sp, color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("gym_day_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = FitnessRose,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                            ),
                            maxLines = 2,
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Save details button
                        Button(
                            onClick = {
                                viewModel.saveGymDayDetails(
                                    selectedDateStr,
                                    workoutType,
                                    ratingValue.toInt(),
                                    noteText
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FitnessGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Gym Journal Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun iOSCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    gymDays: List<GymDay>,
    onDayClick: (Int) -> Unit
) {
    val totalDays = yearMonth.lengthOfMonth()
    val firstDay = LocalDate.of(yearMonth.year, yearMonth.month, 1)
    // dayOfWeek value is 1 (Mon) to 7 (Sun)
    // For standard calendar view starting Sunday, offset is firstDay.dayOfWeek.value % 7
    val offset = firstDay.dayOfWeek.value % 7

    val daysOfWeekLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkGreyWidget)
            .padding(14.dp)
    ) {
        // Days of week row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeekLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total grid cells = offset + totalDays
        val totalGridSlots = offset + totalDays
        val rows = (totalGridSlots / 7) + (if (totalGridSlots % 7 != 0) 1 else 0)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (r in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (c in 0..6) {
                        val slotIndex = (r * 7) + c
                        if (slotIndex < offset || slotIndex >= offset + totalDays) {
                            // Empty slot
                            Box(modifier = Modifier.size(38.dp))
                        } else {
                            val dayNum = slotIndex - offset + 1
                            val dateAtSlot = LocalDate.of(yearMonth.year, yearMonth.month, dayNum)
                            val dateAtSlotStr = dateAtSlot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                            // Checks if this day has a gym visit logged
                            val gymVisit = gymDays.firstOrNull { it.dateString == dateAtSlotStr }
                            val isSelected = selectedDate == dateAtSlot

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> FitnessRose
                                            gymVisit != null -> FitnessGreen.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isSelected -> Color.White
                                            gymVisit != null -> FitnessGreen
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { onDayClick(dayNum) }
                                    .testTag("calendar_day_$dayNum"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || gymVisit != null) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            gymVisit != null -> FitnessGreen
                                            else -> Color.White
                                        }
                                    )

                                    if (gymVisit != null && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(FitnessGreen, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
