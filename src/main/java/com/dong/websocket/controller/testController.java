package com.dong.websocket.controller;

import com.dong.websocket.enity.Mybody;
import com.dong.websocket.utils.JSONChange;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: websocket
 * @description:
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-08-22 14:02
 **/
@RestController
@RequestMapping("api/v1")
public class testController {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @GetMapping("/test")
    void sendrabbimq() {
        Mybody message = new Mybody();
        message.setRoomname("room1");
        message.setMessage("hahaha");
        try {
            rabbitTemplate.convertAndSend("dongdong","dong-broadcast", JSONChange.objToJson(message));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
