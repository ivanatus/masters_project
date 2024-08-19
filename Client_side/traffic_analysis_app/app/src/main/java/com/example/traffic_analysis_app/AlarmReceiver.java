package com.example.traffic_analysis_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the notification title and message from the intent
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Send the notification
        NotificationHelper.sendNotification(context, title, message);
    }
}
