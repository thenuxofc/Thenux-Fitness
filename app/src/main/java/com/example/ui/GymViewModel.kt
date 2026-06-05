package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GymRepository
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Current selected date String ("yyyy-MM-dd")
    private val _selectedDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Triggered iOS-style notification toast simulation
    private val _simulatedNotification = MutableStateFlow<String?>(null)
    val simulatedNotification: StateFlow<String?> = _simulatedNotification.asStateFlow()

    // Light/Dark Theme Preference (defaults to Dark=true, matching premium iOS aesthetic)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    init {
        val database = GymDatabase.getDatabase(application)
        repository = GymRepository(database.gymDao)

        // Seed initial data asynchronously on launch if database is empty
        viewModelScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    // --- Flows ---
    val allGymDays: StateFlow<List<GymDay>> = repository.allGymDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<GymReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workout tasks for the currently selected date
    val todaysTasks: StateFlow<List<WorkoutTask>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Users and Swapping Profiles
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedUser: StateFlow<User?> = repository.selectedUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Statistics Calculations ---
    val gymStats: StateFlow<GymStats> = allGymDays.map { days ->
        calculateStats(days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GymStats())

    // Set selected date
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date.format(dateFormatter)
    }

    // Toggle theme
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        val label = if (_isDarkTheme.value) "Dark Space Space Mode" else "Premium iOS Light Mode"
        sendToastWithSystemNotification("Theme Switched", "Active palette: $label")
    }

    // Integrated Toast + sound notifications
    fun sendToastWithSystemNotification(title: String, message: String) {
        _simulatedNotification.value = "$title: $message"
        com.example.NotificationHelper.sendNotification(getApplication(), title, message)
    }

    // Create custom user profile
    fun createUserAndSelect(name: String, weight: Double = 75.0, height: Double = 180.0) {
        viewModelScope.launch {
            val user = User(
                name = name,
                weightKg = weight,
                heightCm = height,
                isSelectedUserProfile = true
            )
            repository.insertUser(user)
            val direct = repository.getSelectedUserDirectly()
            if (direct != null) {
                repository.selectUser(direct.id)
            }
            sendToastWithSystemNotification("Gym User Profile Created", "Welcome to Thenux, $name! Loaded targets.")
        }
    }

    // Select Active user profile
    fun selectUserProfile(userId: Int) {
        viewModelScope.launch {
            repository.selectUser(userId)
            val selected = repository.getSelectedUserDirectly()
            selected?.let {
                sendToastWithSystemNotification("Athlete Shift", "Active Trained Athlete: ${it.name}")
            }
        }
    }

    // Delete user profile
    fun deleteUserProfile(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
            sendToastWithSystemNotification("Athlete Removed", "Profile ${user.name} deleted.")
        }
    }

    // Toggle gym attendance on the selected date
    fun toggleGymAttendance(date: LocalDate, rating: Int = 5, workoutType: String = "General") {
        viewModelScope.launch {
            val dateStr = date.format(dateFormatter)
            val existing = repository.getGymDayByDate(dateStr)
            if (existing != null) {
                repository.deleteGymDay(existing)
                sendToastWithSystemNotification("Gym Attendance Terminated", "Removed login check-in for $dateStr.")
            } else {
                repository.insertGymDay(
                    GymDay(
                        dateString = dateStr,
                        rating = rating,
                        workoutType = workoutType,
                        notes = "Completed at Thenux Fitness"
                    )
                )
                sendToastWithSystemNotification("Gym Login Confirmed!", "Added attendance for $dateStr. Daily streak updated!")
            }
        }
    }

    // Update or insert rating and notes for date
    fun saveGymDayDetails(dateStr: String, workoutType: String, rating: Int, notes: String) {
        viewModelScope.launch {
            val existing = repository.getGymDayByDate(dateStr)
            if (existing != null) {
                repository.insertGymDay(existing.copy(
                    workoutType = workoutType,
                    rating = rating,
                    notes = notes
                ))
            } else {
                repository.insertGymDay(GymDay(
                    dateString = dateStr,
                    workoutType = workoutType,
                    rating = rating,
                    notes = notes
                ))
            }
            sendToastWithSystemNotification("Workout Log Saved", "Details preserved successfully.")
        }
    }

    // Remove gym attendance strictly
    fun removeGymAttendance(dateStr: String) {
        viewModelScope.launch {
            repository.deleteGymDayByDate(dateStr)
            sendToastWithSystemNotification("Check-In Cleared", "Cleared gym attendance for $dateStr.")
        }
    }

    // --- Reminders Actions ---
    fun addReminder(title: String, timeString: String, daysOfWeek: String, category: String) {
        viewModelScope.launch {
            repository.insertReminder(
                GymReminder(
                    title = title,
                    timeString = timeString,
                    daysOfWeek = daysOfWeek,
                    category = category
                )
            )
            sendToastWithSystemNotification("Workout Alarm Created", "Reminder for '$title' set at $timeString.")
        }
    }

    fun deleteReminder(reminder: GymReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            sendToastWithSystemNotification("Alarm Defused", "Removed alert for ${reminder.title}.")
        }
    }

    fun toggleReminderStatus(id: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateReminderStatus(id, isEnabled)
        }
    }

    // Force trigger simulation of a reminder (iOS style push banner)
    fun simulateReminder(title: String, body: String) {
        sendToastWithSystemNotification(title, body)
    }

    fun dismissNotification() {
        _simulatedNotification.value = null
    }

    // --- Tasks Actions ---
    fun addTask(title: String, category: String) {
        viewModelScope.launch {
            repository.insertTask(
                WorkoutTask(
                    title = title,
                    category = category,
                    dateString = _selectedDate.value
                )
            )
            sendToastWithSystemNotification("Workout Element Created", "Added '$title' to today's schedule.")
        }
    }

    fun toggleTask(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, isCompleted)
            if (isCompleted) {
                sendToastWithSystemNotification("Exercise Checked Off!", "Checked off. Phenomenal training mechanics!")
            }
        }
    }

    fun deleteTask(task: WorkoutTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
            sendToastWithSystemNotification("Exercise Extinguished", "Deleted task '${task.title}' from history.")
        }
    }

    fun loadDefaultWorkoutSet(setCategory: String) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value
            // clear current first
            repository.clearTasksForDate(dateStr)

            val defaults = when (setCategory) {
                "Chest & Arms" -> listOf(
                    WorkoutTask(title = "Push-ups Warmup (3 sets)", category = "Chest & Arms", dateString = dateStr),
                    WorkoutTask(title = "Barbell Bench Press (4x8)", category = "Chest & Arms", dateString = dateStr),
                    WorkoutTask(title = "Incline Dumbbell Flyes (3x12)", category = "Chest & Arms", dateString = dateStr),
                    WorkoutTask(title = "Dumbbell Bicep Curls (3x10)", category = "Chest & Arms", dateString = dateStr),
                    WorkoutTask(title = "Tricep Overhead Extension (3x12)", category = "Chest & Arms", dateString = dateStr)
                )
                "Legs & Core" -> listOf(
                    WorkoutTask(title = "Barbell Squats (4x8)", category = "Legs & Core", dateString = dateStr),
                    WorkoutTask(title = "Leg Press Machine (3x12)", category = "Legs & Core", dateString = dateStr),
                    WorkoutTask(title = "Calf Raises (4x15)", category = "Legs & Core", dateString = dateStr),
                    WorkoutTask(title = "Plank Hold (3x 60 seconds)", category = "Legs & Core", dateString = dateStr),
                    WorkoutTask(title = "Romanian Deadlifts (3x10)", category = "Legs & Core", dateString = dateStr)
                )
                "Cardio Spark" -> listOf(
                    WorkoutTask(title = "Treadmill Run (20 mins)", category = "Cardio Spark", dateString = dateStr),
                    WorkoutTask(title = "Stationary Cycling (15 mins)", category = "Cardio Spark", dateString = dateStr),
                    WorkoutTask(title = "Battle Ropes Circuit (3 mins)", category = "Cardio Spark", dateString = dateStr),
                    WorkoutTask(title = "Rowing Machine Sprint", category = "Cardio Spark", dateString = dateStr)
                )
                else -> listOf(
                    WorkoutTask(title = "Dynamic full body stretch", category = "General", dateString = dateStr),
                    WorkoutTask(title = "Hydrate at Thenux Fountain", category = "General", dateString = dateStr),
                    WorkoutTask(title = "Post-workout cooldown", category = "General", dateString = dateStr)
                )
            }

            defaults.forEach { repository.insertTask(it) }
        }
    }

    // --- Helper calculation methods ---
    private fun calculateStats(days: List<GymDay>): GymStats {
        if (days.isEmpty()) return GymStats()

        val parsedDates = days.mapNotNull {
            try {
                LocalDate.parse(it.dateString, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.sortedDescending() // newest first

        // 1. Calculate Active Streak
        var streak = 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val dateSet = parsedDates.toSet()

        if (dateSet.contains(today) || dateSet.contains(yesterday)) {
            var curr = if (dateSet.contains(today)) today else yesterday
            streak = 1
            while (true) {
                val next = curr.minusDays(1)
                if (dateSet.contains(next)) {
                    streak++
                    curr = next
                } else {
                    break
                }
            }
        }

        // 2. Monthly gym count (current month)
        val currentMonth = today.month
        val currentYear = today.year
        val monthlyCount = parsedDates.count {
            it.month == currentMonth && it.year == currentYear
        }

        // 3. Favourite Workout Type
        val favType = days.filter { it.workoutType.isNotBlank() && it.workoutType != "General" }
            .groupBy { it.workoutType }
            .maxByOrNull { it.value.size }?.key ?: "Strength & Cardio"

        return GymStats(
            streak = streak,
            monthlyCount = monthlyCount,
            favWorkoutType = favType,
            totalCount = days.size
        )
    }

    private suspend fun seedInitialDataIfNeeded() {
        // We observe flows or do an initial fetch
        val existingReminders = repository.allReminders.firstOrNull() ?: emptyList()
        if (existingReminders.isEmpty()) {
            repository.insertReminder(GymReminder(title = "Morning Gym Boost", timeString = "06:30 AM", daysOfWeek = "Mon, Wed, Fri", category = "Strength"))
            repository.insertReminder(GymReminder(title = "Leg Day Grind", timeString = "05:45 PM", daysOfWeek = "Tue, Thu", category = "Legs & Core"))
            repository.insertReminder(GymReminder(title = "Weekend Cardio Special", timeString = "09:00 AM", daysOfWeek = "Sat", category = "Cardio Spark"))
            repository.insertReminder(GymReminder(title = "Post-Gym Deep Stretch", timeString = "08:15 PM", daysOfWeek = "Daily", category = "Stretching"))
        }

        val existingDays = repository.allGymDays.firstOrNull() ?: emptyList()
        if (existingDays.isEmpty()) {
            val today = LocalDate.now()
            // Let's seed a few past workout days in the last month
            repository.insertGymDay(GymDay(dateString = today.minusDays(1).format(dateFormatter), workoutType = "Chest & Arms", rating = 5, notes = "Awesome energy, beat my bench press record!"))
            repository.insertGymDay(GymDay(dateString = today.minusDays(3).format(dateFormatter), workoutType = "Legs & Core", rating = 4, notes = "Heavy squats left my legs sore but highly accomplished!"))
            repository.insertGymDay(GymDay(dateString = today.minusDays(4).format(dateFormatter), workoutType = "Cardio Spark", rating = 5, notes = "Smashed 5K treadmill run."))
            repository.insertGymDay(GymDay(dateString = today.minusDays(7).format(dateFormatter), workoutType = "Chest & Arms", rating = 3, notes = "Felt a bit tired today but THENUX fitness guidelines kept me going."))
        }

        // Preload default tasks for today if empty
        val todayStr = LocalDate.now().format(dateFormatter)
        val existingTasks = repository.getTasksForDate(todayStr).firstOrNull() ?: emptyList()
        if (existingTasks.isEmpty()) {
            repository.insertTask(WorkoutTask(title = "Double scoop pre-workout drink", category = "Preparation", isCompleted = true, dateString = todayStr))
            repository.insertTask(WorkoutTask(title = "Assisted pullups warm-up (3x10)", category = "Strength", isCompleted = false, dateString = todayStr))
            repository.insertTask(WorkoutTask(title = "15 minutes high-intensity cardio", category = "Cardio Spark", isCompleted = false, dateString = todayStr))
            repository.insertTask(WorkoutTask(title = "Dumbbell chest flyes (4x12)", category = "Strength", isCompleted = false, dateString = todayStr))
            repository.insertTask(WorkoutTask(title = "Wipe down machines - Thenux Etiquette", category = "Etiquette", isCompleted = false, dateString = todayStr))
        }
    }
}

data class GymStats(
    val streak: Int = 0,
    val monthlyCount: Int = 0,
    val favWorkoutType: String = "Strength & Cardio",
    val totalCount: Int = 0,
    val monthlyTarget: Int = 12 // default target count per month
)
