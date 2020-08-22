package com.dong.websocket.enity;

/**
 * @program: websocket
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-08-22 14:08
 **/
public class Mybody {
    String roomname;
    String message;

    public Mybody() {
    }

    public Mybody(String roomname, String message) {
        this.roomname = roomname;
        this.message = message;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "roomname='" + roomname + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
