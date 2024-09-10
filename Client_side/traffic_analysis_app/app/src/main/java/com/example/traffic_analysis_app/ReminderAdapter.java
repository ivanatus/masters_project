package com.example.traffic_analysis_app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;
    private Context context;

    public ReminderAdapter(Context context, List<Reminder> reminders) {
        this.reminders = reminders;
        this.context = context;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.title.setText(reminder.title);
        holder.description.setText(reminder.description);
        holder.time.setText(reminder.hour + ":" + String.format("%02d", reminder.minute));
        String[] days = new String[reminder.daysOfWeek.length];
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < reminder.daysOfWeek.length; i++){
            days[i] = getDayOfWeekString(Integer.parseInt(reminder.daysOfWeek[i]));
            Log.d("ReminderAdapter", days[i]);
            stringBuilder.append(days[i]);
            if(i < reminder.daysOfWeek.length - 1) {
                stringBuilder.append(", ");
            }
        }
        //holder.daysOftheWeek.setText("Ponavlja se na " + getDayOfWeekString(reminder.daysOfWeek));
        holder.daysOftheWeek.setText("Ponavlja se na " + stringBuilder);

        holder.deleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a list of scheduled days
                List<Integer> scheduledDays = new ArrayList<>();
                for (String day : reminder.daysOfWeek) {
                    scheduledDays.add(Integer.parseInt(day));
                }

                /*SettingsActivity settingsActivity = new SettingsActivity();
                settingsActivity.deleteReminder(scheduledDays, reminder.hour, reminder.minute, reminder.title, reminder.description);*/
                ((SettingsActivity) context).deleteReminder(scheduledDays, reminder.hour, reminder.minute, reminder.title, reminder.description);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time, daysOftheWeek;
        ImageButton deleteReminder;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            description = itemView.findViewById(R.id.textDescription);
            time = itemView.findViewById(R.id.textTime);
            daysOftheWeek = itemView.findViewById(R.id.daysOfTheWeek);
            deleteReminder = itemView.findViewById(R.id.deleteButton);
        }
    }

    private String getDayOfWeekString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "ponedjeljak";
            case Calendar.TUESDAY:
                return "utorak";
            case Calendar.WEDNESDAY:
                return "srijeda";
            case Calendar.THURSDAY:
                return "četvrtak";
            case Calendar.FRIDAY:
                return "petak";
            case Calendar.SATURDAY:
                return "subota";
            case Calendar.SUNDAY:
                return "nedjelja";
            default:
                return "";
        }
    }

    private int getNumberOfWeekString(String dayOfWeek) {
        switch (dayOfWeek) {
            case "ponedjeljak":
                return Calendar.MONDAY;
            case "utorak":
                return Calendar.TUESDAY;
            case "srijeda":
                return Calendar.WEDNESDAY;
            case "četvrtak":
                return Calendar.THURSDAY;
            case "petak":
                return Calendar.FRIDAY;
            case "subota":
                return Calendar.SATURDAY;
            case "nedjelja":
                return Calendar.SUNDAY;
            default:
                return 0;
        }
    }
}
