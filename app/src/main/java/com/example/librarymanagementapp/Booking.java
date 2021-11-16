package com.example.librarymanagementapp;

public class Booking {

    private String date, time, room, name, email;

    public Booking(){

    }

    public Booking(String date, String time, String room, String name, String email){
        this.date = date;
        this.time = time;
        this.room = room;
        this.name = name;
        this.email = email;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
