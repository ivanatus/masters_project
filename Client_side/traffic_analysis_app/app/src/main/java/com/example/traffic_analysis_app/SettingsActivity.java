package com.example.traffic_analysis_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextDescription;
    private TimePicker timePicker;
    private CheckBox checkBoxMonday, checkBoxTuesday, checkBoxWednesday, checkBoxThursday, checkBoxFriday, checkBoxSaturday, checkBoxSunday;
    private Button buttonSetReminder, buttonDeleteReminder;
    boolean showToolbarMenu = false;
    BottomNavigationView bottom_navigation;
    RecyclerView recyclerView;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }


    //adding the navigation bar to the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        if (!showToolbarMenu)
            return false;
        getMenuInflater().inflate(R.menu.bottom_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //defining the action that is done after navigation bar/toolbar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.recording) {
            Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(recording);
            finish();
        } else if(id == R.id.info) {
            //userInstructions(this);
            Intent data = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(data);
            finish();
        } else if(id == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.home){
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        // Clear any selected item
        bottom_navigation.setOnNavigationItemSelectedListener(null);
        bottom_navigation.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottom_navigation.getMenu().size(); i++) {
            bottom_navigation.getMenu().getItem(i).setChecked(false);
        }
        bottom_navigation.getMenu().setGroupCheckable(0, true, true);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.recording){
                    Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
                    startActivity(recording);
                    finish();
                } else if(id == R.id.info){
                    Log.d("ANOVASTATS", "ID recognized");
                    Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                    startActivity(info);
                    finish();
                } else if (id == R.id.home) {
                    Intent home = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(home);
                    finish();
                }
                return false;
            }
        });

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        timePicker = findViewById(R.id.timePicker);
        checkBoxMonday = findViewById(R.id.checkBoxMonday);
        checkBoxTuesday = findViewById(R.id.checkBoxTuesday);
        checkBoxWednesday = findViewById(R.id.checkBoxWednesday);
        checkBoxThursday = findViewById(R.id.checkBoxThursday);
        checkBoxFriday = findViewById(R.id.checkBoxFriday);
        checkBoxSaturday = findViewById(R.id.checkBoxSaturday);
        checkBoxSunday = findViewById(R.id.checkBoxSunday);
        LinearLayout newReminderSetup = findViewById(R.id.newReminderSetup);

        buttonSetReminder = findViewById(R.id.buttonSetReminder);
        buttonDeleteReminder = findViewById(R.id.buttonDeleteReminder);

        buttonDeleteReminder.setOnClickListener(v -> deleteReminders());

        recyclerView = findViewById(R.id.reminderList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ReminderAdapter(loadReminders()));

        FloatingActionButton newReminder = findViewById(R.id.newReminder);
        newReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newReminder.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                newReminderSetup.setVisibility(View.VISIBLE);
            }
        });

        buttonSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setReminders();
                newReminderSetup.setVisibility(View.GONE);
                newReminder.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setReminders() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        /*if (checkBoxMonday.isChecked()) {
            scheduleReminder(Calendar.MONDAY, hour, minute, title, description);
            checkBoxMonday.setChecked(false);
        }
        if (checkBoxTuesday.isChecked()) {
            scheduleReminder(Calendar.TUESDAY, hour, minute, title, description);
            checkBoxTuesday.setChecked(false);
        }
        if (checkBoxWednesday.isChecked()) {
            scheduleReminder(Calendar.WEDNESDAY, hour, minute, title, description);
            checkBoxWednesday.setChecked(false);
        }
        if (checkBoxThursday.isChecked()) {
            scheduleReminder(Calendar.THURSDAY, hour, minute, title, description);
            checkBoxThursday.setChecked(false);
        }
        if (checkBoxFriday.isChecked()) {
            scheduleReminder(Calendar.FRIDAY, hour, minute, title, description);
            checkBoxFriday.setChecked(false);
        }
        if (checkBoxSaturday.isChecked()) {
            scheduleReminder(Calendar.SATURDAY, hour, minute, title, description);
            checkBoxSaturday.setChecked(false);
        }
        if (checkBoxSunday.isChecked()) {
            scheduleReminder(Calendar.SUNDAY, hour, minute, title, description);
            checkBoxSunday.setChecked(false);
        }*/

        // Collect all the selected days
        List<Integer> selectedDays = new ArrayList<>();
        if (checkBoxMonday.isChecked()) selectedDays.add(Calendar.MONDAY);
        if (checkBoxTuesday.isChecked()) selectedDays.add(Calendar.TUESDAY);
        if (checkBoxWednesday.isChecked()) selectedDays.add(Calendar.WEDNESDAY);
        if (checkBoxThursday.isChecked()) selectedDays.add(Calendar.THURSDAY);
        if (checkBoxFriday.isChecked()) selectedDays.add(Calendar.FRIDAY);
        if (checkBoxSaturday.isChecked()) selectedDays.add(Calendar.SATURDAY);
        if (checkBoxSunday.isChecked()) selectedDays.add(Calendar.SUNDAY);

        // Schedule a single reminder that repeats on the selected days
        if (!selectedDays.isEmpty()) {
            scheduleReminder(selectedDays, hour, minute, title, description);
            // Clear checkboxes after setting reminder
            checkBoxMonday.setChecked(false);
            checkBoxTuesday.setChecked(false);
            checkBoxWednesday.setChecked(false);
            checkBoxThursday.setChecked(false);
            checkBoxFriday.setChecked(false);
            checkBoxSaturday.setChecked(false);
            checkBoxSunday.setChecked(false);
        }

        recyclerView.setAdapter(new ReminderAdapter(loadReminders()));
        editTextTitle.setText("");
        editTextDescription.setText("");
    }

    private void scheduleReminder(List<Integer> daysOfWeek, int hour, int minute, String title, String description) {
        /*Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1); // If the time is before now, schedule for next week
        }*/

        Calendar calendar = null;
        for (int dayOfWeek : daysOfWeek) {
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1); // If the time is before now, schedule for next week
            }

            // Schedule the repeating reminder for each selected day
            ReminderManager.setReminder(this, title, description, calendar, AlarmManager.INTERVAL_DAY * 7);
        }

        // Schedule the repeating reminder using ReminderManager
        //ReminderManager.setReminder(this, title, description, calendar, AlarmManager.INTERVAL_DAY * 7);
        saveReminder(daysOfWeek, hour, minute, title, description);
    }

    private void deleteReminders() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        if (checkBoxMonday.isChecked()) {
            cancelReminder(Calendar.MONDAY, hour, minute);
        }
        if (checkBoxTuesday.isChecked()) {
            cancelReminder(Calendar.TUESDAY, hour, minute);
        }
        if (checkBoxWednesday.isChecked()) {
            cancelReminder(Calendar.WEDNESDAY, hour, minute);
        }
        if (checkBoxThursday.isChecked()) {
            cancelReminder(Calendar.THURSDAY, hour, minute);
        }
        if (checkBoxFriday.isChecked()) {
            cancelReminder(Calendar.FRIDAY, hour, minute);
        }
        if (checkBoxSaturday.isChecked()) {
            cancelReminder(Calendar.SATURDAY, hour, minute);
        }
        if (checkBoxSunday.isChecked()) {
            cancelReminder(Calendar.SUNDAY, hour, minute);
        }
    }

    private void saveReminder(List<Integer> daysOfWeek, int hour, int minute, String title, String description) {
        SharedPreferences sharedPreferences = getSharedPreferences("Reminders", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Create a unique key for each reminder
        //String reminderKey = dayOfWeek + "_" + hour + "_" + minute;

        // Store the reminder details in a single string
        //String reminderDetails = dayOfWeek + ";" + hour + ";" + minute + ";" + title + ";" + description;

        StringBuilder daysString = new StringBuilder();
        for (int day : daysOfWeek) {
            daysString.append(day).append(","); // Append each day
        }

        // Remove the last comma
        if (daysString.length() > 0) {
            daysString.deleteCharAt(daysString.length() - 1);
        }

        // Create a unique key for each reminder
        String reminderKey = title + ";" + description + ";" + daysString + ";" + hour + ";" + minute;


        // Save to SharedPreferences
        editor.putString(reminderKey, reminderKey);
        editor.apply();
    }


    private void cancelReminder(int dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        ReminderManager.cancelReminder(this, calendar);
    }

    private List<Reminder> loadReminders() {
        SharedPreferences sharedPreferences = getSharedPreferences("Reminders", MODE_PRIVATE);
        Map<String, ?> allReminders = sharedPreferences.getAll();
        List<Reminder> reminderList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allReminders.entrySet()) {
            String[] reminderDetails = ((String) entry.getValue()).split(";");
            /*int dayOfWeek = Integer.parseInt(reminderDetails[0]);
            int hour = Integer.parseInt(reminderDetails[1]);
            int minute = Integer.parseInt(reminderDetails[2]);
            String title = reminderDetails[3];
            String description = reminderDetails[4];

            reminderList.add(new Reminder(dayOfWeek, hour, minute, title, description));*/

            // Ensure the reminderDetails array has the expected number of elements (5 in this case)
            if (reminderDetails.length == 5) {
                try {
                    String daysString = reminderDetails[0];
                    //int dayOfWeek = Integer.parseInt(reminderDetails[0]);
                    int hour = Integer.parseInt(reminderDetails[1]);
                    int minute = Integer.parseInt(reminderDetails[2]);
                    String title = reminderDetails[3];
                    String description = reminderDetails[4];

                    // Split daysString into individual days
                    String[] daysArray = daysString.split(",");
                    for (String day : daysArray) {
                        int dayOfWeek = Integer.parseInt(day);
                        reminderList.add(new Reminder(daysArray, hour, minute, title, description));
                    }

                    //reminderList.add(new Reminder(daysArray, hour, minute, title, description));
                } catch (NumberFormatException e) {
                    // Handle the case where the data cannot be parsed into an integer
                    Log.e("ReminderLoading", "Failed to parse reminder data: " + entry.getValue(), e);
                }
            } else {
                // Handle the case where the data does not have the expected format
                Log.e("ReminderLoading", "Unexpected reminder format: " + entry.getValue());
            }
        }

        return reminderList;
    }

    private void clearAllReminders() {
        SharedPreferences sharedPreferences = getSharedPreferences("Reminders", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all reminders
        editor.apply();  // Apply the changes
    }
}