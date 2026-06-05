package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: GymViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToWorkouts: () -> Unit
) {
    val stats by viewModel.gymStats.collectAsState()
    val allGymDays by viewModel.allGymDays.collectAsState()
    val todaysTasks by viewModel.todaysTasks.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    val todayDate = LocalDate.now()
    val todayStr = todayDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val isCheckedInToday = allGymDays.any { it.dateString == todayStr }

    val completedTasks = todaysTasks.count { it.isCompleted }
    val totalTasks = todaysTasks.size

    // State controllers for User custom creation sheet/dialog
    var showUserCreator by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    var newUserWeight by remember { mutableStateOf("75") }
    var newUserHeight by remember { mutableStateOf("180") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Premium iOS Title & Creator Header (Includes Thenux Fitness Custom Logo) ---
        HeaderSection(selectedUser = selectedUser)

        Spacer(modifier = Modifier.height(14.dp))

        // --- Custom Theme Changer & Quick Utilities Dashboard Card ---
        ThemeAndNotificationControls(
            isDark = isDark,
            onToggleTheme = { viewModel.toggleTheme() },
            onTriggerNotification = {
                viewModel.sendToastWithSystemNotification(
                    "Thenux Fitness Alert",
                    "Alert triggered! It is session training time. Open your workout checklist."
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Active Athlete Switcher & ID Registry Widget (Users Creator) ---
        AthleteRegistrySection(
            selectedUser = selectedUser,
            allUsers = allUsers,
            showUserCreator = showUserCreator,
            newUserName = newUserName,
            newUserWeight = newUserWeight,
            newUserHeight = newUserHeight,
            onToggleCreator = { showUserCreator = !showUserCreator },
            onUserNameChange = { newUserName = it },
            onUserWeightChange = { newUserWeight = it },
            onUserHeightChange = { newUserHeight = it },
            onRegisterAthlete = {
                if (newUserName.isNotBlank()) {
                    val w = newUserWeight.toDoubleOrNull() ?: 75.0
                    val h = newUserHeight.toDoubleOrNull() ?: 180.0
                    viewModel.createUserAndSelect(newUserName, w, h)
                    // Reset fields
                    newUserName = ""
                    showUserCreator = false
                }
            },
            onSwitchUser = { userId ->
                viewModel.selectUserProfile(userId)
            },
            onDeleteUser = { user ->
                viewModel.deleteUserProfile(user)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Custom Cohesive Concentric Rings Widget ---
        val taskProgress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
        val monthlyProgress = stats.monthlyCount.toFloat() / stats.monthlyTarget.coerceAtLeast(1)
        val streakProgress = stats.streak.toFloat() / 7f // benchmark to weekly 7 days

        iOSActivityRingsCard(
            streakProgress = streakProgress,
            monthlyProgress = monthlyProgress,
            taskProgress = taskProgress,
            stats = stats,
            completedTasks = completedTasks,
            totalTasks = totalTasks
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- iOS Style Gym Ticker check-in button ---
        QuickCheckInCard(
            isCheckedIn = isCheckedInToday,
            onToggle = {
                viewModel.toggleGymAttendance(todayDate, 5, "General Fitness")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Highlight Statistics Row ---
        GridStatsSection(stats = stats, onNavigateToCalendar = onNavigateToCalendar, onNavigateToWorkouts = onNavigateToWorkouts)

        Spacer(modifier = Modifier.height(14.dp))

        // --- Amazing Guide to Thenux Fitness ---
        AmazingGuideSection()

        Spacer(modifier = Modifier.height(14.dp))

        // --- THENUX Creator Signature Badge ---
        CreatorDetailsCard()
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun HeaderSection(selectedUser: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "THENUX FITNESS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = FitnessRose,
                modifier = Modifier.testTag("app_title")
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Creator",
                    tint = FitnessBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val athleteName = selectedUser?.name ?: "Guest Athlete"
                Text(
                    text = "Trained: $athleteName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = Color.Gray
                )
            }
        }

        // Render dynamic Thenux Fitness Dynamic Logo from localized res disk
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(6.dp, CircleShape)
                .border(2.dp, FitnessRose, CircleShape)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.thenux_fitness_logo),
                contentDescription = "Thenux Fitness Logo File",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ThemeAndNotificationControls(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onTriggerNotification: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Theme Icon",
                        tint = if (isDark) FitnessCyan else FitnessOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isDark) "Active: Cosmic dark" else "Active: iOS Premium light",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Smooth styled switch
                Switch(
                    checked = !isDark,
                    onCheckedChange = { onToggleTheme() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = FitnessOrange,
                        checkedTrackColor = FitnessOrange.copy(alpha = 0.3f),
                        uncheckedThumbColor = FitnessCyan,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }

            Divider(color = Color.Gray.copy(alpha = 0.15f))

            Button(
                onClick = onTriggerNotification,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FitnessBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Alert Icon",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Trigger System Alarm (With Sound)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AthleteRegistrySection(
    selectedUser: User?,
    allUsers: List<User>,
    showUserCreator: Boolean,
    newUserName: String,
    newUserWeight: String,
    newUserHeight: String,
    onToggleCreator: () -> Unit,
    onUserNameChange: (String) -> Unit,
    onUserWeightChange: (String) -> Unit,
    onUserHeightChange: (String) -> Unit,
    onRegisterAthlete: () -> Unit,
    onSwitchUser: (Int) -> Unit,
    onDeleteUser: (User) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Athletes",
                        tint = FitnessRose,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Athletes Profile Switcher",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onToggleCreator,
                    modifier = Modifier
                        .size(28.dp)
                        .background(FitnessRose.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (showUserCreator) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Athlete",
                        tint = FitnessRose,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Expose quick switcher list
            if (allUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tap to switch active profile:",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Render list rows of created users
                allUsers.forEach { user ->
                    val isCurrent = user.isSelectedUserProfile || (selectedUser?.id == user.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrent) FitnessRose.copy(alpha = 0.1f)
                                else Color.Gray.copy(alpha = 0.05f)
                            )
                            .border(
                                1.dp,
                                if (isCurrent) FitnessRose.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSwitchUser(user.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isCurrent) FitnessRose else Color.Gray,
                                        CircleShape
                                    )
                            )
                            Column {
                                Text(
                                    text = user.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Height: ${user.heightCm}cm | Weight: ${user.weightKg}kg",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Delete specific profile button
                        IconButton(
                            onClick = { onDeleteUser(user) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No user profiles found. Tap '+' to create our active profile!",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = showUserCreator) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "REGISTER NEW ATHLETE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FitnessRose
                    )

                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = onUserNameChange,
                        label = { Text("Athlete Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitnessRose,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedLabelColor = FitnessRose
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newUserWeight,
                            onValueChange = onUserWeightChange,
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FitnessRose,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedLabelColor = FitnessRose
                            )
                        )

                        OutlinedTextField(
                            value = newUserHeight,
                            onValueChange = onUserHeightChange,
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FitnessRose,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                focusedLabelColor = FitnessRose
                            )
                        )
                    }

                    Button(
                        onClick = onRegisterAthlete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = FitnessRose),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add Profile & Save Live", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AmazingGuideSection() {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Guide",
                        tint = FitnessGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Amazing Gym App Guide",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Collapse Indicator",
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GuideItemChip(
                        badgeColor = FitnessRose,
                        title = "1. Routine Training Checklist",
                        body = "Swap to the 'Checklist' tab from the bottom navigation. Tap 'Add custom tasks' or tap the preset buttons (Chest & Arms, Legs & Core, Cardio Spark) to load instant target exercises. Tap checkboxes to persist completion instantly!"
                    )

                    GuideItemChip(
                        badgeColor = FitnessGreen,
                        title = "2. Monthly Log Calendar",
                        body = "Swap to the 'Calendar' tab. Tap on any calendar slot to check in or log notes and ratings of your gym workouts. Checked in days glow with high-intensity pink. Long streak values are computed live."
                    )

                    GuideItemChip(
                        badgeColor = FitnessBlue,
                        title = "3. Daily Notification Alarms",
                        body = "Swap to 'Reminders'. Tap '+' to append precise notifications. The application handles periodic reminders to prompt you on training days with custom alarm and vibrating sound!"
                    )

                    GuideItemChip(
                        badgeColor = FitnessOrange,
                        title = "4. Athletes Directory Caching",
                        body = "Using the core 'Athletes Profile Switcher' card above, registers custom family members or friends. Dynamic height/weight calculations scale stats in Room persistence instantly."
                    )
                }
            }
        }
    }
}

@Composable
fun GuideItemChip(badgeColor: Color, title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Gray.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(badgeColor, CircleShape)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 11.sp,
            color = Color.Gray,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun iOSActivityRingsCard(
    streakProgress: Float,
    monthlyProgress: Float,
    taskProgress: Float,
    stats: GymStats,
    completedTasks: Int,
    totalTasks: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Legend and values
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Activity Rings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Streak Ring Legend
                LegendRow(
                    color = FitnessRose,
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Move (Streak)",
                    status = "${stats.streak}d / 7d"
                )

                // Monthly Ring Legend
                LegendRow(
                    color = FitnessGreen,
                    icon = Icons.Default.CalendarMonth,
                    label = "Exercise (Month)",
                    status = "${stats.monthlyCount} / ${stats.monthlyTarget} days"
                )

                // Tasks Ring Legend
                LegendRow(
                    color = FitnessBlue,
                    icon = Icons.Default.TaskAlt,
                    label = "Stand (Today)",
                    status = "$completedTasks / $totalTasks completed"
                )
            }

            // Right Box: Concentric Canvas
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                ActivityRingsCanvas(
                    streakProgress = streakProgress,
                    monthlyProgress = monthlyProgress,
                    taskProgress = taskProgress
                )
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, icon: ImageVector, label: String, status: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
            Text(text = status, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActivityRingsCanvas(
    streakProgress: Float,
    monthlyProgress: Float,
    taskProgress: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val strokeWidth = 14.dp.toPx()
        val spacing = 18.dp.toPx()

        // Ring radii from outside to inside
        val r1 = (size.minDimension / 2f) - (strokeWidth / 2f)
        val r2 = r1 - spacing
        val r3 = r2 - spacing

        // Colors
        val trackAlpha = 0.15f

        // 1. Move Ring (Streak - Rose)
        drawCircle(
            color = FitnessRose.copy(alpha = trackAlpha),
            radius = r1,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = FitnessRose,
            startAngle = -90f,
            sweepAngle = (streakProgress * 360f).coerceIn(0f, 360f),
            useCenter = false,
            topLeft = Offset(center.x - r1, center.y - r1),
            size = Size(r1 * 2, r1 * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 2. Exercise Ring (Monthly Days - Green)
        drawCircle(
            color = FitnessGreen.copy(alpha = trackAlpha),
            radius = r2,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = FitnessGreen,
            startAngle = -90f,
            sweepAngle = (monthlyProgress * 360f).coerceIn(0f, 360f),
            useCenter = false,
            topLeft = Offset(center.x - r2, center.y - r2),
            size = Size(r2 * 2, r2 * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 3. Stand Ring (Today Tasks - Blue)
        drawCircle(
            color = FitnessBlue.copy(alpha = trackAlpha),
            radius = r3,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = FitnessBlue,
            startAngle = -90f,
            sweepAngle = (taskProgress * 360f).coerceIn(0f, 360f),
            useCenter = false,
            topLeft = Offset(center.x - r3, center.y - r3),
            size = Size(r3 * 2, r3 * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun QuickCheckInCard(
    isCheckedIn: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggle() }
            .testTag("quick_check_in_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCheckedIn) FitnessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCheckedIn) FitnessGreen.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ticking circle container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isCheckedIn) FitnessGreen else Color.Gray.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCheckedIn) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                        contentDescription = "Check-in Status",
                        tint = if (isCheckedIn) Color.Black else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isCheckedIn) "Already Visited Gym Today!" else "Go to Gym Today?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isCheckedIn) "Marked in calendar! Tap to undo check-in." else "Tap to log check-in for June 5th!",
                        color = if (isCheckedIn) FitnessGreen else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = if (isCheckedIn) Icons.Default.Verified else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isCheckedIn) FitnessGreen else Color.Gray
            )
        }
    }
}

@Composable
fun GridStatsSection(
    stats: GymStats,
    onNavigateToCalendar: () -> Unit,
    onNavigateToWorkouts: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Stat Card: Streak
        Card(
            modifier = Modifier
                .weight(1f)
                .height(130.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigateToCalendar() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = FitnessRose,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Calendar",
                        fontSize = 11.sp,
                        color = FitnessRose,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "${stats.streak} Days",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Current Streak",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Right Stat Card: Monthly Target
        Card(
            modifier = Modifier
                .weight(1f)
                .height(130.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { onNavigateToWorkouts() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Completion",
                        tint = FitnessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Workouts",
                        fontSize = 11.sp,
                        color = FitnessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "${stats.monthlyCount}/${stats.monthlyTarget}",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Monthly Goal",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun CreatorDetailsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(FitnessBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsBasketball,
                        contentDescription = "Creator Logo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Creator Info",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Designed by THENUX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))

            Text(
                text = "Thenux Fitness utility provides a robust high-fidelity workspace inspired by premium iOS health app interfaces. Complete with reactive SQLite Room caching, real-time metrics, custom alarms, and calendar journaling.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = Color.Gray
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Version 1.0.0 (Stable)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "THENUX Studio",
                    fontSize = 10.sp,
                    color = FitnessBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
