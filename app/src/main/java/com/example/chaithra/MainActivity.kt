package com.example.chaithra

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.*
import com.example.chaithra.ui.screens.*
import com.example.chaithra.ui.theme.ManojTheme
import com.example.chaithra.viewmodel.LibraryViewModel
import com.example.chaithra.worker.DeadlineWorker
import java.util.concurrent.TimeUnit
import com.example.chaithra.utils.TranslatorUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- Initialize Notification Worker ---
        setupDeadlineWorker()

        // --- Pre-download Kannada Translation Model ---
        try {
            TranslatorUtils.prepareModel()
        } catch (e: Exception) {
            android.util.Log.e("Main", "Translator failed to init: ${e.message}")
        }

        setContent {
            ManojTheme {
                RequestNotificationPermission()
                MainApp()
            }
        }
    }

    private fun setupDeadlineWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DeadlineWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deadline_reminder_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted -> /* Handle permission result if needed */ }
        )
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.AccountCircle)
    object Home : Screen("home", "Books", Icons.Default.Home)
    object Scan : Screen("scan", "Scan", Icons.Default.Search)
    object History : Screen("history", "History", Icons.Default.DateRange)
    object Students : Screen("students", "Students", Icons.Default.Person)
    object Leaderboard : Screen("leaderboard", "Ranking", Icons.Default.List)
    object AddBook : Screen("add_book", "Add", Icons.Default.Add)
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: LibraryViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val isTeacher by viewModel.isTeacherMode.collectAsState()

    val items = remember(isTeacher) {
        if (isTeacher) {
            listOf(Screen.Home, Screen.Scan, Screen.History, Screen.Students, Screen.AddBook, Screen.Leaderboard)
        } else {
            listOf(Screen.Home, Screen.Scan, Screen.Leaderboard)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route && currentRoute != "insights") {
                NavigationBar(tonalElevation = 8.dp) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(viewModel, navController) }
            composable(Screen.Home.route) { HomeScreen(viewModel, navController) }
            composable(Screen.Scan.route) { ScannerScreen(viewModel, navController) }
            composable(Screen.History.route) { HistoryScreen(viewModel, navController) }
            composable(Screen.Students.route) { StudentScreen(viewModel, navController) }
            composable(Screen.Leaderboard.route) { LeaderboardScreen(viewModel, navController) }
            composable(Screen.AddBook.route) { AddBookScreen(viewModel, navController) }

            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                BookDetailScreen(bookId, viewModel, navController)
            }

            // --- LIBRARY INSIGHTS ROUTE ---
            composable("insights") {
                LibraryInsightsScreen(viewModel = viewModel, navController = navController)
            }
        }
    }
}