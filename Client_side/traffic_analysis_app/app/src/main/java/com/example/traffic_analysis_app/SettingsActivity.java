package com.example.traffic_analysis_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Field;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextDescription;
    private TimePicker timePicker;
    private CheckBox checkBoxMonday, checkBoxTuesday, checkBoxWednesday, checkBoxThursday, checkBoxFriday, checkBoxSaturday, checkBoxSunday;
    private Button buttonSetReminder, buttonDeleteReminder;
    boolean showToolbarMenu = false;
    BottomNavigationView bottom_navigation;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void removeOverflowMenu(Menu menu) {
        try {
            Field field = MenuBuilder.class.getDeclaredField("mOptionalIconsVisible");
            field.setAccessible(true);
            field.set(menu, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        buttonSetReminder = findViewById(R.id.buttonSetReminder);
        buttonDeleteReminder = findViewById(R.id.buttonDeleteReminder);

        buttonSetReminder.setOnClickListener(v -> setReminders());
        buttonDeleteReminder.setOnClickListener(v -> deleteReminders());
    }

    private void setReminders() {
        editTextTitle.setText("");
        editTextDescription.setText("");

        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        if (checkBoxMonday.isChecked()) {
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
        }
    }

    private void scheduleReminder(int dayOfWeek, int hour, int minute, String title, String description) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1); // If the time is before now, schedule for next week
        }

        // Schedule the repeating reminder using ReminderManager
        ReminderManager.setReminder(this, title, description, calendar, AlarmManager.INTERVAL_DAY * 7);
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

    private void cancelReminder(int dayOfWeek, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        ReminderManager.cancelReminder(this, calendar);
    }
}