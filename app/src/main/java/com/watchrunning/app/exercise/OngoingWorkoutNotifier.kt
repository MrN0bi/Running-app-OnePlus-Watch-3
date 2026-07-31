package com.watchrunning.app.exercise

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import com.watchrunning.app.R
import com.watchrunning.app.app.MainActivity
import com.watchrunning.app.calculation.MetricFormatters
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState

class OngoingWorkoutNotifier(private val context: Context) {
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun build(state: WorkoutUiState): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = when (state.phase) {
            WorkoutPhase.Preparing -> context.getString(R.string.notification_preparing)
            WorkoutPhase.Paused, WorkoutPhase.Pausing -> context.getString(R.string.notification_paused)
            else -> context.getString(
                R.string.notification_running,
                MetricFormatters.distanceKilometres(state.metrics.distanceMetres),
            )
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_run_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        OngoingActivity.Builder(context, NOTIFICATION_ID, builder)
            .setStaticIcon(R.drawable.ic_run_notification)
            .setTouchIntent(pendingIntent)
            .setTitle(context.getString(R.string.app_name))
            .build()
            .apply(context)
        return builder.build()
    }

    fun notify(state: WorkoutUiState) {
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, build(state))
    }

    fun cancel() {
        context.getSystemService(NotificationManager::class.java).cancel(NOTIFICATION_ID)
    }

    companion object {
        const val CHANNEL_ID = "workout"
        const val NOTIFICATION_ID = 42
    }
}
