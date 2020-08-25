package com.dong.websocket.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @program: websocket
 * @description: 从容器中取对象的工具类
 * @author: ywd
 * @contact:1371690483@qq.com
 * @create: 2020-08-25 10:47
 **/

@Component
public class SpringUtil implements ApplicationContextAware{

        private static ApplicationContext applicationContext ;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            SpringUtil.applicationContext = applicationContext;
        }

        public ApplicationContext getApplicationContext(){
            return applicationContext;
        }

        @SuppressWarnings("unchecked")
        public static Object getBean(String beanName){
            return applicationContext.getBean(beanName);
        }
        @SuppressWarnings("unchecked")
        public static <T> T getBean(Class<T> clazz){
            return (T)applicationContext.getBean(clazz);
        }

        private static void checkApplicationContext() {
            if (applicationContext == null) {
                throw new IllegalStateException(
                        "applicationContext未注入,请从启动类main方法中通过SpringApplication.run方法获取到的返回值设置给本类的applicationContext变量");
            }
        }

}
