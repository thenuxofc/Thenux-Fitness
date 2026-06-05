package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDark) {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

enum class FitnessScreen {
    Dashboard,
    Calendar,
    Workouts,
    Reminders
}

@Composable
fun MainAppContainer(viewModel: GymViewModel) {
    var currentTab by remember { mutableStateOf(FitnessScreen.Dashboard) }
    val notificationState by viewModel.simulatedNotification.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()

    // Slide down notification capsule handler
    LaunchedEffect(notificationState) {
        if (notificationState != null) {
            // Auto-dismiss after 6 seconds
            delay(6000)
            viewModel.dismissNotification()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Core Screen Routing ---
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                // Customized Premium Floating iOS bottom navigation
                iOSFloatingTabBar(
                    activeTab = currentTab,
                    onTabSelected = { currentTab = it },
                    isDark = isDark
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 12.dp) // extra padding to avoid clipping floating bottom bar
            ) {
                when (currentTab) {
                    FitnessScreen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCalendar = { currentTab = FitnessScreen.Calendar },
                        onNavigateToWorkouts = { currentTab = FitnessScreen.Workouts }
                    )
                    FitnessScreen.Calendar -> CalendarScreen(viewModel = viewModel)
                    FitnessScreen.Workouts -> WorkoutsScreen(viewModel = viewModel)
                    FitnessScreen.Reminders -> RemindersScreen(viewModel = viewModel)
                }
            }
        }

        // --- Custom Animated iOS Pull-Down Push notification ---
        AnimatedVisibility(
            visible = notificationState != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { -it }
            ) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .zIndex(99f) // Keep on TOP of everything
        ) {
            notificationState?.let { msg ->
                iOSPushCapsule(
                    message = msg,
                    onDismiss = { viewModel.dismissNotification() }
                )
            }
        }
    }
}

@Composable
fun iOSFloatingTabBar(
    activeTab: FitnessScreen,
    onTabSelected: (FitnessScreen) -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(if (isDark) Color(0xFF141414).copy(alpha = 0.95f) else Color(0xFFFFFFFF).copy(alpha = 0.95f)) // Frosted theme-aware iOS effect
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .testTag("floating_tab_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabBarItem(
                screen = FitnessScreen.Dashboard,
                icon = Icons.Default.TrendingUp,
                label = "Dashboard",
                isActive = activeTab == FitnessScreen.Dashboard,
                color = FitnessRose,
                onClick = { onTabSelected(FitnessScreen.Dashboard) }
            )

            TabBarItem(
                screen = FitnessScreen.Calendar,
                icon = Icons.Default.CalendarMonth,
                label = "Calendar",
                isActive = activeTab == FitnessScreen.Calendar,
                color = FitnessGreen,
                onClick = { onTabSelected(FitnessScreen.Calendar) }
            )

            TabBarItem(
                screen = FitnessScreen.Workouts,
                icon = Icons.Default.FitnessCenter,
                label = "Checklist",
                isActive = activeTab == FitnessScreen.Workouts,
                color = FitnessBlue,
                onClick = { onTabSelected(FitnessScreen.Workouts) }
            )

            TabBarItem(
                screen = FitnessScreen.Reminders,
                icon = Icons.Default.NotificationsActive,
                label = "Reminders",
                isActive = activeTab == FitnessScreen.Reminders,
                color = FitnessOrange,
                onClick = { onTabSelected(FitnessScreen.Reminders) }
            )
        }
    }
}

@Composable
fun TabBarItem(
    screen: FitnessScreen,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag("tab_bar_item_${screen.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) color else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}

@Composable
fun iOSPushCapsule(
    message: String,
    onDismiss: () -> Unit
) {
    val parts = message.split(": ", limit = 2)
    val title = parts.getOrNull(0) ?: "Thenux Fitness"
    val body = parts.getOrNull(1) ?: "Alarm Checklist trigger Notification"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .testTag("push_notification_capsule"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(20.dp)
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
                // Gym App Icon Bubble
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(FitnessRose, FitnessOrange)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "App Alarm",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = body,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray,
                        lineHeight = 16.sp
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close simulation alert",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
