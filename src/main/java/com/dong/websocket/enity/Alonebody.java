package com.dong.websocket.enity;

/**
 * @program: websocket
 * @description: 发送到个人实体类
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-08-22 15:43
 **/
public class Alonebody {
    String roomname;
    String userid;
    String message;

    public Alonebody(String roomname, String userid, String message) {
        this.roomname = roomname;
        this.userid = userid;
        this.message = message;
    }

    public Alonebody() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @Override
    public String toString() {
        return "Alonebody{" +
                "roomname='" + roomname + '\'' +
                ", userid='" + userid + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
