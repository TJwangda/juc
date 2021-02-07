package com.test.juc.rwLock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 独占锁（写锁） 一次只能被一个线程持有
 * 共享锁（读锁） 多个线程可以同事占有
 * readwriteLock
 * 读-读 可以共存
 * 读-写  不可以共存
 * 写-写  不可以共存
 */
public class ReadWriteLockDemo {
    public static void main(String[] args) {
//        MyCache myCache = new MyCache();
        MyCacheLock myCache = new MyCacheLock();
        //写入
        for(int i = 1;i<= 5;i++){
            final int temp = i;
            new Thread(()->{
                myCache.put(temp+"",temp+"");
            },String.valueOf(i)).start();
        }
//        获取
        for(int i = 1;i<= 5;i++){
            final int temp = i;
            new Thread(()->{
                myCache.get(temp+"");
            },String.valueOf(i)).start();
        }

    }
}

/**
 * 无锁，多天线程插入影响逻辑。
 * 自定义缓存，set/get
 *
 */
class MyCache{
    private volatile Map<String,Object> map= new HashMap();
    //存
    public void put(String key,Object value){
        System.out.println(Thread.currentThread().getName()+"写入"+key);
        map.put(key,value);
        System.out.println(Thread.currentThread().getName()+"写完ok"+key);
    }
    //取
    public void get(String key){
        System.out.println(Thread.currentThread().getName()+"读取"+key);
        Object o = map.get(key);
        System.out.println(Thread.currentThread().getName()+"读取ok"+key);
//        return o;
    }
}

/**
 * 自定义缓存-有锁，set/get
 *
 */
class MyCacheLock{
    private volatile Map<String,Object> map= new HashMap();
    //读写锁，更加细粒度控制
    private ReadWriteLock readWriteLock =  new ReentrantReadWriteLock();
//    private Lock lock = new ReentrantLock();
    //存，写入时只希望一个时间只有一个线程写入
    public void put(String key,Object value){
//        lock.lock();
        readWriteLock.writeLock().lock();//加写锁
        try {
            System.out.println(Thread.currentThread().getName()+"写入"+key);
            map.put(key,value);
            System.out.println(Thread.currentThread().getName()+"写完ok"+key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.writeLock().unlock();//解写锁
        }
    }
    //取，读可以所有人读取
    public void get(String key){
        readWriteLock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+"读取"+key);
            Object o = map.get(key);
            System.out.println(Thread.currentThread().getName()+"读取ok"+key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readWriteLock.readLock().unlock();
        }
//        return o;
    }
}