package com.test.juc.unSafe;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MapTest {
    public static void main(String[] args) {
        //map平常怎么用，
        // 默认等价于什么 new HashMap<>() ==》new HashMap<>(16,0.75)
//        Map<String, String> map = new HashMap<>();
        Map<String, String> map = new ConcurrentHashMap<>();//方案一
        // 加载因子，初始化容量  稍后查一下

        for(int i = 1 ;i <= 30 ;i++){
            new Thread(()->{
                map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0,5));
                System.out.println(map);
            },String.valueOf(i)).start();
        }


    }
}
