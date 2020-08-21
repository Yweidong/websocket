package com.dong.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @program: websocket
 * @description:
 * @author: ywd
 * @create: 2020-08-18 16:05
 * @param roomname 房间号
 * @param userId  用户id
 **/
@ServerEndpoint("/websocket/{roomname}/{userId}")
@Component

public class WebSocketServer {
    private static final Logger logger =LoggerFactory.getLogger(WebSocketServer.class);
    //静态变量，用来记录当前房间在线连接数。应该把它设计成线程安全的。
    private static Map<String,Integer> map = null;

    //concurrent包的线程安全Set
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap();

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;


    //接收sid
    private String userId="";

    /**
     *
     *建立连接
     */
    @OnOpen
    public void onOpen(@PathParam("roomname") String roomname,@PathParam("userId") String userId, Session session)
    {

        this.userId = userId;
        this.session = session;
//
//        webSocketSet.add(this);
        if(rooms.containsKey(roomname)) {
            // 房间已存在，直接添加用户到相应的房间
            rooms.get(roomname).add(session);

        }else {
            Set<Session> room = new HashSet<Session>();
            //添加用户
            room.add(session);

            rooms.put(roomname,room);
        }


        try {
            sendMessage("连接成功1111");
            logger.info("房间号【"+roomname+"】有新的连接, 总数:{"+rooms.get(roomname).size()+"}");

        }
        catch (Exception e){
            logger.info("websocket IO异常");

        }


    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("roomname") String roomname,Session session,@PathParam("userId") String userId) {

        rooms.get(roomname).remove(session);

        logger.info("房间号【"+roomname+"】中用户【"+userId+"】连接断开, 总数:{"+rooms.get(roomname).size()+"}");
    }

    @OnError
    public void onError(Session session, Throwable error) {

        logger.info("发生错误");
        error.printStackTrace();
    }

    /**
     *
     * rabbitmq还未完善，测试需注释掉
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,
                    exchange =@Exchange(value = "dong",type="fanout")
            )
    })
    @RabbitHandler
    @OnMessage
    public void onMessage(@PathParam("roomname") String roomname,String message, Session session) {
        logger.info("收到来自房间"+roomname+"的信息:"+message);
        for (Session session1 : rooms.get(roomname)) {
            try {
                session1.getBasicRemote().sendText(message);//服务器主动推送
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     *
     *发送到具体某个人
     */
    public static void sendInfo(@PathParam("roomname") String roomname,
                                String message,@PathParam("userId") String userId,Session session) throws IOException {

        session.getBasicRemote().sendText(message);

    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
