package com.example.killua4564.hwapplication4;

import com.google.firebase.database.Exclude;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class Item {

    private String id;
    private String classname;
    private String week;
    private int hour;
    private int minute;
    private boolean key;

    public Item(String id, String classname, String week, int hour, int minute, boolean key) {
        this.id = id;
        this.classname = classname;
        this.week = week;
        this.hour = hour;
        this.minute = minute;
        this.key = key;
    }

    public String getId() {
        return this.id;
    }

    public String getClassname() {
        return this.classname;
    }

    public String getWeek() {
        return this.week;
    }

    public int getHour() {return this.hour;}

    public int getMinute() {return this.minute;}

    public boolean getKey() {
        return this.key;
    }

    public void setKey(boolean key) {
        this.key = key;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("classname", this.classname);
        result.put("week", this.week);
        result.put("hour", String.valueOf(this.hour));
        result.put("minute", String.valueOf(this.minute));
        result.put("key", this.key);
        return result;
    }
}
