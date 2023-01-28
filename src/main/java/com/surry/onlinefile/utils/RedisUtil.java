package com.surry.onlinefile.utils;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 将Redis的一些对象转换集合起来
 */
@Component
public class RedisUtil {

    /**
     * 使用Autowired装载的时候，如果有多个同类型的bean
     * 那么需要标明是哪个方法生产出的bean
     */
    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisUtil.redisTemplate = redisTemplate;
    }

    /**
     * 将一个对象以hash类型写入到redis中
     * 参数是一个键名和一个对象
     * 不需要返回值
     */
    public static void saveBean(Object key, Object o) throws Exception {

        // 通过反射获取o中的变量名
        Field[] fields = o.getClass().getDeclaredFields();
        // 循环找到每一个方法中的
        for (Field field : fields) {
            // 获取属性名字，并将首字符改成大写
            String name = field.getName();
            String old = name;
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            // 获取对应的get方法
            Method method = o.getClass().getMethod("get" + name);
            // 执行get方法获取值
            Object value = method.invoke(o);
            System.out.println(old + " " + value);
            // 名字和值写入到Redis中
            System.out.println(key);
            redisTemplate.opsForHash().put(key, old, value);
        }

    }

    /**
     * 通过key获取hash类型数据并将数据放入到对象中返回
     * 传入的参数有key，和一个想要初始化的对象
     */
    public static Object findBean(Object key, Object o) throws Exception {

        // 通过key和属性名可以获取到值
        Field[] fields = o.getClass().getDeclaredFields();
        // 获取每一个的值
        for (Field field : fields) {
            // 获取到名字
            String name = field.getName();
            // 值的类型，因为后续传参需要
            Class type = field.getType();
            // 通过名字调用o的set方法
            String old = name;
            // 将首字母大写
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            // 获取对应的set方法，因为是有参数的，所以需要参数类型
            Method method = o.getClass().getMethod("set" + name, new Class[]{type});
            // 执行方法
            // 获取redis中的值的时候使用原来的名字，也就是old
            Object object = redisTemplate.opsForHash().get(key, old);
//            if(object != null) {
//                System.out.println("tostring"+object.getClass().toString());
//            }
            if(object != null && "class java.lang.String".equals(object.getClass().toString())) {
                // 检查格式
//                System.out.println("------------" + object.toString());
                try{
                    LocalDateTime localDateTime = LocalDateTime.parse(object.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    method.invoke(o, localDateTime);
                    continue;
                } catch (Exception e) {
                    // 不做操作
                }
            }
//            System.out.println(redisTemplate.opsForHash().get(key, old).getClass());
            method.invoke(o, redisTemplate.opsForHash().get(key, old));
        }
        // 封装好就返回
        return o;

    }

}
