package com.dong.websocket.server;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @program: websocket
 * @description:
 * @author: zxb
 * @create: 2020-08-18 16:05
 **/
@ServerEndpoint("/websocket/{userId}")
@Component
public class webSocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<webSocketServer> webSocketSet = new CopyOnWriteArraySet<webSocketServer>();

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
        addOnlineCount();
        webSocketSet.add(this);

        try {
            sendMessage("连接成功");

        }
        catch (IOException e){
//            logger.info(userId+"上线的时候通知所有人发生了错误");
        }


    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
    }

    @OnError
    public void onError(Session session, Throwable error) {
//        log.error("发生错误");
        error.printStackTrace();
    }


    @OnMessage
    public void onMessage(String message, Session session) {
//        log.info("收到来自窗口"+sid+"的信息:"+message);
        //群发消息
        for (webSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendInfo(String message,@PathParam("sid") String userId) throws IOException {
//        log.info("推送消息到窗口"+sid+"，推送内容:"+message);
        for (webSocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if (userId == null) {
                    item.sendMessage(message);
                } else if (item.userId.equals(userId)) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }


        /**
         * 实现服务器主动推送
         */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);//同步消息
    }

    private static synchronized void addOnlineCount() {
        webSocketServer.onlineCount++;
    }
    private static synchronized void subOnlineCount() {
        webSocketServer.onlineCount--;
    }

}
