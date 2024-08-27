package com.example.traffic_analysis_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminders;

    public ReminderAdapter(List<Reminder> reminders) {
        this.reminders = reminders;
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
        for(int i = 0; i < reminder.daysOfWeek.length; i++){
            days[i] = getDayOfWeekString(Integer.parseInt(reminder.daysOfWeek[i])) + " ";
        }
        //holder.daysOftheWeek.setText("Ponavlja se na " + getDayOfWeekString(reminder.daysOfWeek));
        holder.daysOftheWeek.setText("Ponavlja se na " + days);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, time, daysOftheWeek;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            description = itemView.findViewById(R.id.textDescription);
            time = itemView.findViewById(R.id.textTime);
            daysOftheWeek = itemView.findViewById(R.id.daysOfTheWeek);
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
}
