package com.example.manoj.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.manoj.viewmodel.LibraryViewModel
import java.util.concurrent.TimeUnit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.manoj.worker.DeadlineWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: LibraryViewModel, navController: NavController) {
    val isTeacher by viewModel.isTeacherMode.collectAsState()
    val currentStudent by viewModel.currentStudent.collectAsState()
    val books by viewModel.allBooks.collectAsState()
    val myBorrowedBooks by viewModel.myBorrowedBooks.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Story", "Science", "History", "Literature")

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        val titleText = if (isTeacher) "Librarian Portal" else "Namaskara, ${currentStudent?.name ?: "Student"}"
                        Text(titleText, fontWeight = FontWeight.Bold)
                        if (!isTeacher) {
                            Text("Your Rural Library Assistant", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (isTeacher) {
                FloatingActionButton(
                    onClick = { navController.navigate("add_book") },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Book")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // --- LIBRARIAN INSIGHTS SECTION ---
            if (isTeacher) {
                Button(
                    onClick = { navController.navigate("insights") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Library Insights & Analytics", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))

                // TEMPORARY TEST BUTTON
                Button(
                    onClick = {
                        val testRequest = OneTimeWorkRequestBuilder<DeadlineWorker>().build()
                        WorkManager.getInstance(navController.context).enqueue(testRequest)
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("DEBUG: Trigger Notification Check", color = Color.White)
                }
            }

            // --- STUDENT PERSONAL SECTION ---
            if (!isTeacher && myBorrowedBooks.isNotEmpty()) {
                Text(
                    text = "My Borrowed Books",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    items(myBorrowedBooks) { tx ->
                        val currentTime = System.currentTimeMillis()
                        val diff = tx.dueDate - currentTime
                        val daysLeft = Math.ceil(diff.toDouble() / (1000 * 60 * 60 * 24)).toLong()
                        val statusColor = if (daysLeft < 0) MaterialTheme.colorScheme.error
                        else if (daysLeft < 3) Color(0xFFFFA000) // Orange warning
                        else MaterialTheme.colorScheme.primary

                        Card(
                            modifier = Modifier.width(200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = statusColor.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(tx.bookTitle, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (daysLeft < 0) "Overdue!" else "Due in $daysLeft days",
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Divider(Modifier.padding(bottom = 16.dp), thickness = 0.5.dp)
            }

            // --- SEARCH & FILTERS ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title or author...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOOK GRID ---
            if (books.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isTeacher) "Library is empty." else "No books found.",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                val filteredBooks = books.filter {
                    (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                            (it.title.contains(searchQuery, true) || it.author.contains(searchQuery, true))
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookItem(book = book, onClick = {
                            navController.navigate("book_detail/${book.id}")
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun BookItem(book: com.example.manoj.data.BookEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl.isNotEmpty()) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        book.title.take(1).uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = if (book.isIssued) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (book.isIssued) "Issued" else "Available",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = book.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = "${book.pages} pgs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}