package com.example.manoj.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.manoj.viewmodel.LibraryViewModel
import androidx.compose.foundation.horizontalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryInsightsScreen(viewModel: LibraryViewModel, navController: NavController) {
    val popular by viewModel.popularBooks.collectAsState()
    val peaks by viewModel.peakHours.collectAsState()

    // Calculate quick stats
    val totalIssues = popular.sumOf { it.second }
    val topBook = popular.firstOrNull()?.first ?: "None"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Library Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Summary Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), label = "Total Issues", value = "$totalIssues")
                StatCard(modifier = Modifier.weight(1f), label = "Top Book", value = topBook)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Usage Patterns",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Popular Books with Dynamic Scaling
            AnalyticsSectionCard(title = "Most Borrowed Books") {
                if (popular.isEmpty()) {
                    Text("No data yet.", modifier = Modifier.padding(16.dp))
                } else {
                    val maxIssues = popular.maxOfOrNull { it.second } ?: 1
                    popular.forEach { (title, count) ->
                        PopularBookRow(title = title, count = count, maxIssues = maxIssues)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Peak Borrowing Hours with AM/PM Formatting
            AnalyticsSectionCard(title = "Peak Activity (Daily Timeline)") {
                if (peaks.isEmpty()) {
                    Text("Activity will appear after books are issued.", modifier = Modifier.padding(16.dp))
                } else {
                    PeakHoursChart(peaks = peaks.sortedBy { it.first }) // Sort by hour
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun PopularBookRow(title: String, count: Int, maxIssues: Int) {
    // Dynamic scaling: The most popular book always gets a full bar
    val progress by animateFloatAsState(
        targetValue = count.toFloat() / maxIssues,
        animationSpec = tween(durationMillis = 1000)
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                Text("$count Issues", modifier = Modifier.padding(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun PeakHoursChart(peaks: List<Pair<Int, Int>>) {
    // 1. Filter out empty hours and sort them chronologically
    val processedPeaks = remember(peaks) {
        peaks.filter { it.second > 0 }.sortedBy { it.first }
    }
    val maxCount = processedPeaks.maxOfOrNull { it.second } ?: 1

    // 2. We use a Box with a horizontalScroll so labels never get squished
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()) // Ensures labels are never cut off
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            processedPeaks.forEach { (hour, count) ->
                val barHeight by animateFloatAsState(
                    targetValue = (count.toFloat() / maxCount).coerceAtLeast(0.15f),
                    animationSpec = tween(durationMillis = 1000)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp) // Fixed width: This is what keeps the labels visible!
                        .fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // Count Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "$count",
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // The Bar with Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(barHeight)
                            .width(32.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // The Time Label (formatHour handles AM/PM)
                    Text(
                        text = formatHour(hour),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "${hour} AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

@Composable
fun AnalyticsSectionCard(title: String, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            content()
        }
    }
}