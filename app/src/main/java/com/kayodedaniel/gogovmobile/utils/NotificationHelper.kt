package com.kayodedaniel.gogovmobile.utils

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.activities.VerifiedPaymentActivity

class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "payment_channel"
        private const val NOTIFICATION_ID = 1
    }

    // notification channel class
    init {
        createNotificationChannel()
    }

    // creates a notification class channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Payment Notifications"
            val descriptionText = "Notifications for payment status"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // shows the processing notification for payement
    fun showPaymentProcessingNotification() {
        if (!hasNotificationPermission()) {
            requestNotificationPermission()
            return
        }

        val intent = Intent(context, VerifiedPaymentActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // paending intent on payment processing
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Payment Processing")
            .setContentText("Thank you, Payment processing will take 3 days.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            requestNotificationPermission()
        }
    }

    // checks permission for notification on device
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // requests notification permission
    fun requestNotificationPermission() {
        if (!hasNotificationPermission()) {
            showEnableNotificationDialog()
        }
    }

    // enables notification dialog
    private fun showEnableNotificationDialog() {
        AlertDialog.Builder(context)
            .setTitle("Enable Notifications")
            .setMessage("To receive important updates about your payments and applications, please enable notifications in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("Not Now") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_notification)
            .create()
            .show()
    }

    // opens notification settings
    private fun openNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}