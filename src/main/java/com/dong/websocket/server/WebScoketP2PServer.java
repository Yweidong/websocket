package com.dong.websocket.server;

import com.dong.websocket.enity.Alonebody;
import com.dong.websocket.utils.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: 杨伟栋
 * @Date: 2020/8/25 20:41
 * @Description: 1371690483@qq.com    个人到个人，相当于私聊
 * @param userId 发送给目的地人的id
 */
@ServerEndpoint("/websocket/{userId}")
@Component
@DependsOn("springUtil")
public class WebScoketP2PServer {
    private RedisTemplate redisTemplate = (RedisTemplate) SpringUtil.getBean("redisTemplate");

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    //userId 和session之间的映射关系
    private static final Map<Session,String> map = new ConcurrentHashMap<>();
    private static final Map<String,Session> smap = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;


    //接收sid
    private String userId="";


    /**
     *
     *建立连接
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session)
    {
        this.userId = userId;
        this.session = session;

        if(!map.containsKey(session)) {
            map.put(session,userId);
        }

        if(!smap.containsKey(userId)) {
            smap.put(userId,session);
        }
        sendMessage("连接成功");


    }

    /**
     *
     *发送到具体某个人==需要发送用户的id
     *
     */

    @RabbitListener(queuesToDeclare={@Queue(value = "hello")})
    @RabbitHandler
    @OnMessage
    public  void OnMessage(String message) throws IOException {

        if(map.size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            Alonebody value = mapper.readValue(message, Alonebody.class);
            String message1 = value.getMessage();

            String userid = value.getUserid();

            // 根据用户id查找初session

            if(smap.containsKey(userid)) {

                Session session = smap.get(userid);


                session.getBasicRemote().sendText(message1);


            }



        }else {
            logger.info("暂无连接");
        }


    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session,@PathParam("userId") String userId) {

        map.remove(session);

        smap.remove(userId);
        logger.info("用户【"+userId+"】连接断开");
    }

    @OnError
    public void onError(Session session, Throwable error) {

        logger.info("发生错误");
        error.printStackTrace();
    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
