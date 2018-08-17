package com.tech.areeb.wavelash;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;

public class SchedulerModel implements Serializable {

    private String SchedulerName;
    private String EventType = "None";
    private int ETA = 0;
    private int hour =0, minute =0, ampm =0;
    private boolean bedroomFans, bedroomLights, drawingroomFans, drawingroomLights;

    public SchedulerModel(String SchedulerName, int ETA){
        this.SchedulerName = SchedulerName;
        EventType = "ETA";
        this.ETA = ETA;
    }

    public SchedulerModel(String SchedulerName, int hour, int minute, int ampm){
        this.SchedulerName = SchedulerName;
        EventType = "Alarm";
        this.hour = hour;
        this.minute = minute;
        this.ampm = ampm;
    }

    public String getSchedulerName(){
        return SchedulerName;
    }

    public String getEventType(){
        return EventType;
    }

    public int getETA(){
        return ETA;
    }

    public int getHour(){
        return hour;
    }

    public int getMinute(){
        return minute;
    }

    public int getAmPm(){
        return ampm;
    }

    public void setBedroomFans(boolean bedroomFans){
        this.bedroomFans = bedroomFans;
    }

    public void setBedroomLights(boolean bedroomLights){
        this.bedroomLights = bedroomLights;
    }

    public void setDrawingroomFans(boolean drawingroomFans){
        this.drawingroomFans = drawingroomFans;
    }

    public void setDrawingroomLights(boolean drawingroomLights){
        this.drawingroomLights = drawingroomLights;
    }

    public boolean getBedroomFans(){
        return bedroomFans;
    }

    public boolean getBedroomLights(){
        return bedroomLights;
    }

    public boolean getDrawingroomFans(){
        return drawingroomFans;
    }

    public boolean getDrawingroomLights(){
        return drawingroomLights;
    }


}
