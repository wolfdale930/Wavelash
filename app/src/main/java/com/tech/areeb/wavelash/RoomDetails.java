package com.tech.areeb.wavelash;


public class RoomDetails {

    private int roomDrawable;
    private String roomName;

    public RoomDetails(int image, String name){
        roomDrawable = image;
        roomName = name;
    }

    public int getImageDrawable(){
        return roomDrawable;
    }

    public String getRoomName(){
        return roomName;
    }

}
