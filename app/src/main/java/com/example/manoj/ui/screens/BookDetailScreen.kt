package com.example.manoj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.manoj.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: Long, viewModel: LibraryViewModel, navController: NavController) {
    val books by viewModel.allBooks.collectAsState()
    val book = books.find { it.id == bookId }
    val reviews by viewModel.getReviews(bookId).collectAsState(initial = emptyList())

    // Role and Session observation
    val isTeacher by viewModel.isTeacherMode.collectAsState()
    val currentStudent by viewModel.currentStudent.collectAsState()

    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Book not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. BOOK HEADER (Always Visible) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(book.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("by ${book.author}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                    if (isTeacher) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Inventory Code: ${book.bookCode}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(onClick = {}, label = { Text(book.category) })
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(onClick = {}, label = { Text("${book.pages} Pages") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. CONDITIONAL REVIEW INPUT (Hidden for Teachers) ---
            if (!isTeacher) {
                Text("Review Corner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Share your thoughts after reading", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rate as ${currentStudent?.name}", style = MaterialTheme.typography.labelLarge)

                        // Star Rating Row
                        Row(modifier = Modifier.padding(vertical = 8.dp)) {
                            repeat(5) { index ->
                                IconButton(onClick = { rating = index + 1 }) {
                                    Icon(
                                        imageVector = if (index < rating) Icons.Default.Star else Icons.Outlined.Star,
                                        contentDescription = null,
                                        tint = if (index < rating) Color(0xFFFFD700) else Color.Gray
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { if (it.length <= 100) comment = it },
                            label = { Text("Your Review") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            placeholder = { Text("What did you think of the story?") }
                        )

                        Button(
                            onClick = {
                                if (comment.isNotBlank()) {
                                    viewModel.addReview(book.id, rating, comment)
                                    comment = ""
                                }
                            },
                            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                            enabled = comment.isNotBlank()
                        ) {
                            Text("Submit Review")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 3. REVIEWS LIST (Visible to Everyone) ---
            Text("Community Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (reviews.isEmpty()) {
                Text("No reviews yet. Students can share feedback after reading!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                reviews.forEach { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(review.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.weight(1f))
                                Row {
                                    repeat(review.rating) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Text("\"${review.comment}\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}