package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_days")
data class GymDay(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // format "YYYY-MM-DD"
    val notes: String = "",
    val workoutType: String = "General", // e.g. "Chest & Arms", "Legs", "Cardio"
    val rating: Int = 5, // Rating 1-5
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "gym_reminders")
data class GymReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeString: String, // e.g. "08:30 AM"
    val daysOfWeek: String, // e.g. "Mon, Wed, Fri"
    val isEnabled: Boolean = true,
    val category: String = "General" // e.g. "Strength", "Cardio", "Yoga", "Hydration"
)

@Entity(tableName = "workout_tasks")
data class WorkoutTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "General", // e.g. "Strength", "Cardio", "Stretch"
    val isCompleted: Boolean = false,
    val dateString: String // format "YYYY-MM-DD"
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val joinedDateString: String = "2026-06-05",
    val weightKg: Double = 75.0,
    val heightCm: Double = 180.0,
    val isSelectedUserProfile: Boolean = false
)
