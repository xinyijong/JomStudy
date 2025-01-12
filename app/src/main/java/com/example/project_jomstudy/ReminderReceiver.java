package com.example.project_jomstudy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String assignmentTitle = intent.getStringExtra("assignmentTitle");
        String daysBeforeDue = intent.getStringExtra("daysBeforeDue");

        // Log to check if data is properly received
        Log.d("ReminderReceiver", "Received assignmentTitle: " + assignmentTitle + ", daysBeforeDue: " + daysBeforeDue);

        // Check if the app has the POST_NOTIFICATIONS permission
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed with creating the notification
            String channelId = "assignment_reminders";
            String contentText = assignmentTitle + " is due in " + daysBeforeDue + " days!";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.icon) // Replace with your notification icon
                    .setContentTitle("Assignment Reminder")
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } else {
            // Permission is not granted; log the issue or handle it gracefully
            Log.w("ReminderReceiver", "Permission POST_NOTIFICATIONS is not granted. Notification not shown.");
        }
    }
}
