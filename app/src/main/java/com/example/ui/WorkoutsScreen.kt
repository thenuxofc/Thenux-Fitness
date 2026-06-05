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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutsScreen(viewModel: GymViewModel) {
    val todaysTasks by viewModel.todaysTasks.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf("Strength") }
    var isAddingTask by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val selectedLocalDate = try {
        LocalDate.parse(selectedDateStr, formatter)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val completedCount = todaysTasks.count { it.isCompleted }
    val totalCount = todaysTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0F

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Routine",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Tick off workout metrics as you complete them!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = { isAddingTask = !isAddingTask },
                    modifier = Modifier
                        .size(40.dp)
                        .background(FitnessRose, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isAddingTask) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Exercise",
                        tint = Color.White
                    )
                }
            }
        }

        // --- Custom Workout Templates Grid (Seeders) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGreyWidget),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Download Preset Routine templates",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val templates = listOf(
                            Triple("Chest & Arms", Icons.Default.FitnessCenter, "Chest & Arms"),
                            Triple("Legs & Core", Icons.Default.AirlineSeatLegroomExtra, "Legs & Core"),
                            Triple("Cardio Spark", Icons.Default.DirectionsRun, "Cardio Spark")
                        )

                        templates.forEach { (label, icon, category) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.loadDefaultWorkoutSet(category)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = icon, contentDescription = null, tint = FitnessBlue, modifier = Modifier.size(16.dp))
                                    Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Interactive Progress Bar ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGreyWidget),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedLocalDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = FitnessGreen
                        )
                    }

                    // Progress Track bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(FitnessRose, FitnessGreen)
                                    )
                                )
                        )
                    }

                    Text(
                        text = "$completedCount of $totalCount activities completed today",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- Inline Add Task Form ---
        if (isAddingTask) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGreyWidget),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Add Custom Workout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("e.g. Dumbbell hammer curl 3x12", fontSize = 13.sp, color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("exercise_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = FitnessRose,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true
                        )

                        Text(text = "Routine Category", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val workoutCategories = listOf("Strength", "Legs", "Cardio Spark")
                            workoutCategories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (newTaskCategory == cat) FitnessRose else Color.White.copy(alpha = 0.05f))
                                        .clickable { newTaskCategory = cat }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (newTaskCategory == cat) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTask(newTaskTitle, newTaskCategory)
                                    newTaskTitle = ""
                                    isAddingTask = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("exercise_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = FitnessRose, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Insert Routine Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Task Item Checker list ---
        if (todaysTasks.isEmpty()) {
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
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No activities planned for today",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download a preset routine split or insert one custom!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(todaysTasks, key = { it.id }) { task ->
                WorkoutTaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTask(task.id, !task.isCompleted) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }
    }
}

@Composable
fun WorkoutTaskItem(
    task: WorkoutTask,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag("workout_task_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF1B3D2B) else DarkGreyWidget
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Interactive Check circle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (task.isCompleted) FitnessGreen else Color.White.copy(alpha = 0.08f))
                        .clickable { onToggleCompletion() }
                        .testTag("task_checkbox_click_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.Done else Icons.Default.Circle,
                        contentDescription = "Done",
                        tint = if (task.isCompleted) Color.Black else Color.Transparent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) Color.Gray else Color.White
                    )
                    Text(
                        text = task.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = when (task.category) {
                            "Strength", "Legs", "Chest & Arms" -> FitnessBlue
                            "Cardio Spark" -> FitnessRose
                            else -> FitnessGreen
                        }
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
