package com.test.juc.unSafe;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListTest {
    public static void main(String[] args) {

//        List<String> list = new ArrayList<>();
//        List<String> list = new Vector<>();//方案1、Vector是安全的,不报错
//        List<String> list = Collections.synchronizedList(new ArrayList<>());//方案2、Collections工具类，把集合改成安全
        List<String> list = new CopyOnWriteArrayList<>();//方案3、juc

        //一个线程跑，线程安全
//        for(int i = 1;i<=10;i++){
//            list.add(UUID.randomUUID().toString().substring(0,5));
//            System.out.println(list);
//        }

//        10个线程跑  报错java.util.ConcurrentModificationException，并发修改异常
        //解决方案：方案1、ArrayList换成Vector，Vector是安全的
        //方案2、Collections工具类，把集合改成安全
        //方案3、juc

        //CopyOnWrite 写入时复制， cow 计算机程序设计领域的一种优化策略
        //多个线程调用的时候，list，读取是固定的，写入会覆盖

        for(int i = 1;i<=10;i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,5));
                System.out.println(list);
            },String.valueOf(i)).start();

        }
    }
}
