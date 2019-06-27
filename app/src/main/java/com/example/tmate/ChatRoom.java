package com.example.tmate;

public class ChatRoom {
    private String color_string;
    private String room_name;
    private String last_message;
    private String room_code;

    public ChatRoom(String color_string, String room_name, String room_code, String last_message){
        this.color_string = color_string;
        this.room_name = room_name;
        this.room_code = room_code;
        this.last_message = last_message;
    }

    public String getRoomName(){
        return this.room_name;
    }

    public String getColorString(){
        return this.color_string;
    }

    public String getRoomCode(){
        return this.room_code;
    }

    public String getLastMessage(){
        return this.last_message;
    }
}
