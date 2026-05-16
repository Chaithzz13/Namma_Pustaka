package com.example.manoj.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.manoj.data.LibraryDatabase
import kotlinx.coroutines.flow.first
import kotlin.math.ceil

class DeadlineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = LibraryDatabase.getDatabase(applicationContext)
        val dao = database.dao()

        // Fetch all active transactions
        val transactions = dao.getAllTransactions().first()
        val currentTime = System.currentTimeMillis()

        transactions.forEach { tx ->
            if (!tx.returned) {
                val diff = tx.dueDate - currentTime

                // Using Ceiling Math to round up partial days
                // (e.g., 23 hours becomes 1 day)
                val daysLeft = ceil(diff.toDouble() / (1000 * 60 * 60 * 24)).toInt()

                when {
                    // Scenario 1: Exactly 2 days left
                    daysLeft == 2 -> {
                        sendNotification(
                            id = tx.id.toInt(),
                            title = "Book Due Soon",
                            message = "The book '${tx.bookTitle}' is due in 2 days."
                        )
                    }
                    // Scenario 2: Due tomorrow or within 24 hours
                    daysLeft == 1 -> {
                        sendNotification(
                            id = tx.id.toInt(),
                            title = "Due Tomorrow",
                            message = "The book '${tx.bookTitle}' is due tomorrow. Please return it soon!"
                        )
                    }
                    // Scenario 3: Already overdue
                    daysLeft <= 0 -> {
                        sendNotification(
                            id = tx.id.toInt(),
                            title = "Overdue Alert!",
                            message = "The book '${tx.bookTitle}' is overdue. Please return it to the library immediately."
                        )
                    }
                }
            }
        }
        return Result.success()
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val channelId = "library_deadlines"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Library Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for book return deadlines"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)

        // Using tx.id ensures that multiple borrowed books
        // each get their own separate notification.
        notificationManager.notify(id, builder.build())
    }
}