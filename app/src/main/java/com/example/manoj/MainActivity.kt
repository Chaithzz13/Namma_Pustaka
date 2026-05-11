package com.example.manoj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.manoj.ui.screens.*
import com.example.manoj.ui.theme.ManojTheme
import com.example.manoj.viewmodel.LibraryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ManojTheme {
                MainApp()
            }
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

    // Get the current role state
    val isTeacher by viewModel.isTeacherMode.collectAsState()

    // Dynamically build the navigation items based on the role
    val items = remember(isTeacher) {
        if (isTeacher) {
            listOf(Screen.Home, Screen.Scan, Screen.History, Screen.Students, Screen.AddBook, Screen.Leaderboard)
        } else {
            listOf(Screen.Home, Screen.Scan, Screen.Leaderboard)
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Login.route) {
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

            // FIXED: Added navController here
            composable(Screen.Leaderboard.route) { LeaderboardScreen(viewModel, navController) }

            composable(Screen.AddBook.route) { AddBookScreen(viewModel, navController) }

            composable(
                route = "book_detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                BookDetailScreen(bookId, viewModel, navController)
            }
        }
    }
}