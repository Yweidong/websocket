package com.dong.websocket.server;

import com.dong.websocket.config.RedisConfig;
import com.dong.websocket.enity.Alonebody;
import com.dong.websocket.enity.Mybody;
import com.dong.websocket.utils.JSONChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Headers;
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
   @Autowired
   RedisTemplate redisTemplate;
    private static final Logger logger =LoggerFactory.getLogger(WebSocketServer.class);

    //静态变量，用来记录当前房间在线连接数。应该把它设计成线程安全的。
    private static final Map<String,Map<String,Session>> map = new HashMap<>();

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


        if(rooms.containsKey(roomname)) {
            // 房间已存在，直接添加用户到相应的房间
            rooms.get(roomname).add(session);

        }else {
            Set<Session> room = new HashSet<Session>();
            //添加用户
            room.add(session);

            rooms.put(roomname,room);
        }
        HashMap<String, Session> map1 = new HashMap<>();

        map1.put(userId,session);
        map.put(roomname,map1);




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
     * 广播  rabbitmq 已初步完善
     *  【
     *      1.。。存在问题
     *      后续加入到房间中的人看不到之前发送的广播信息，之后可以加入redis来解决
     *  】
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue,
                        exchange =@Exchange(value = "dongdong",type="direct"),
                    key = {"dong-broadcast"}

            )
    })
    @RabbitHandler

    /**
     * 接受广播消息然后发送
     */
    public void receivebroad(String message,@Headers Map<String,Object> headers, Channel channel) {
            onMessage(message);
            Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
            try {
                channel.basicAck(deliveryTag,false);//手动确认消息，，deliverTag记录接受消息 false不批量接受
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    @OnMessage
    public void onMessage(String message) {

        String roomname = null;
        String message1 = null;
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            Mybody mybody = mapper.readValue(message, Mybody.class);
            roomname = mybody.getRoomname();
            message1 = mybody.getMessage();

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("收到来自房间"+roomname+"的信息:"+message1);
        if(rooms.containsKey(roomname)) {
            for (Session session1 : rooms.get(roomname)) {
                try {
                    session1.getBasicRemote().sendText(message1);//服务器主动推送
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            logger.info("房间号【"+roomname+"】不存在");
        }


    }

    /**
     *
     *发送到具体某个人==需要传房间号和发送用户的id
     *
     */

    @RabbitListener(queuesToDeclare={@Queue(value = "hello")})
    @RabbitHandler
    public  void sendInfo(String message) throws IOException {

        if(map.size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            Alonebody value = mapper.readValue(message, Alonebody.class);
            String message1 = value.getMessage();
            String roomname = value.getRoomname();
            String userid = value.getUserid();

            // 根据用户id查找初session

            if(map.containsKey(roomname)) {
                Session session1 = map.get(roomname).get(userid);
                System.out.println(session1);

                session1.getBasicRemote().sendText(message1);
            }



        }else {
            logger.info("暂无连接");
        }


    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
