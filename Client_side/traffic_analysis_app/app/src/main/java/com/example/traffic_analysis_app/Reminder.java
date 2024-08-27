package com.example.traffic_analysis_app;

public class Reminder {
    //public int dayOfWeek;
    String[] daysOfWeek;
    public int hour;
    public int minute;
    public String title;
    public String description;

    public Reminder(String[] daysOfWeek, int hour, int minute, String title, String description) {
        this.daysOfWeek = daysOfWeek;
        this.hour = hour;
        this.minute = minute;
        this.title = title;
        this.description = description;
    }
}
