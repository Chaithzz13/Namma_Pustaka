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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.manoj.utils.TranslatorUtils
import com.example.manoj.viewmodel.LibraryViewModel
import androidx.compose.ui.draw.scale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(bookId: Long, viewModel: LibraryViewModel, navController: NavController) {
    val books by viewModel.allBooks.collectAsState()
    val book = books.find { it.id == bookId }
    val reviews by viewModel.getReviews(bookId).collectAsState(initial = emptyList())

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
            // --- 1. BOOK HEADER ---
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

                    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        SuggestionChip(onClick = {}, label = { Text(book.category) })
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(onClick = {}, label = { Text("${book.pages} Pages") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TRANSLATABLE DESCRIPTION ---
            TranslatableContent(
                label = "About this Book",
                content = "This book covers interesting topics in ${book.category}. A must-read for students interested in ${book.author}'s work."
                // Note: If you have a real 'description' field in your Book entity, use book.description here instead.
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. REVIEW INPUT (Hidden for Teachers) ---
            if (!isTeacher) {
                Text("Review Corner", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rate as ${currentStudent?.name}", style = MaterialTheme.typography.labelLarge)

                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
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
                            label = { Text("Share your thoughts (English)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (comment.isNotBlank()) {
                                    viewModel.addReview(book.id, rating, comment)
                                    comment = ""
                                }
                            },
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                            enabled = comment.isNotBlank()
                        ) {
                            Text("Submit Review")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 4. REVIEWS LIST ---
            Text("Community Feedback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (reviews.isEmpty()) {
                Text("No reviews yet. Be the first to share!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                reviews.forEach { review ->
                    ReviewItem(review.studentName, review.rating, review.comment)
                }
            }
        }
    }
}

@Composable
fun TranslatableContent(label: String, content: String) {
    var isTranslated by remember { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf(content) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Language Switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ಕನ್ನಡ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Switch(
                    checked = isTranslated,
                    onCheckedChange = { checked ->
                        isTranslated = checked
                        if (checked) {
                            isLoading = true
                            TranslatorUtils.translate(content) { translated ->
                                displayedText = translated
                                isLoading = false
                            }
                        } else {
                            displayedText = content
                        }
                    },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
        } else {
            Text(
                text = displayedText,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = if (isTranslated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ReviewItem(name: String, rating: Int, comment: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    repeat(rating) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Use TranslatableContent for the individual comment
            TranslatableContent(label = "", content = comment)
        }
    }
}

// Add this at the very bottom of BookDetailScreen.kt
