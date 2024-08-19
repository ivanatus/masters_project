package com.example.traffic_analysis_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class ReminderManager {
    private static final String PREFS_NAME = "reminder_prefs";
    private static final String PREF_REMINDER_KEY = "reminder_";

    // Method to set a reminder
    public static void setReminder(Context context, String title, String message, Calendar calendar, long repeatInterval) {
        int requestCode = getUniqueRequestCode(calendar);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    repeatInterval,
                    pendingIntent
            );
        }

        // Save the reminder in SharedPreferences
        saveReminder(context, requestCode, calendar.getTimeInMillis());
    }

    // Generate a unique request code based on the calendar's day, hour, and minute
    private static int getUniqueRequestCode(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_WEEK) * 10000 +
                calendar.get(Calendar.HOUR_OF_DAY) * 100 +
                calendar.get(Calendar.MINUTE);
    }

    // Method to cancel a reminder
    public static void cancelReminder(Context context, Calendar calendar) {
        int requestCode = getUniqueRequestCode(calendar);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // Remove the reminder from SharedPreferences
        removeReminder(context, requestCode);
    }

    // Save reminder to SharedPreferences
    private static void saveReminder(Context context, int requestCode, long timeInMillis) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(PREF_REMINDER_KEY + requestCode, timeInMillis).apply();
    }

    // Remove reminder from SharedPreferences
    private static void removeReminder(Context context, int requestCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(PREF_REMINDER_KEY + requestCode).apply();
    }
}
