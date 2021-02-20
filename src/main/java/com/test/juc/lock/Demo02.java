package com.test.juc.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demo02 {
    /**
     * 可重入锁
     * lock锁
     */
     public static void main(String[] args) {
            Phone2 phone = new Phone2();
            new Thread(()->{
                phone.sms();
            },"a").start();

            new Thread(()->{
                phone.sms();
            },"b").start();
        }
    }

    class Phone2{

        Lock lock = new ReentrantLock();//创建锁
        public  void sms(){
            lock.lock();//上锁
            try {
                System.out.println(Thread.currentThread().getName()+":sms");
                call();//这里也有锁
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();//最后释放
            }

        }

        public  void call(){
            lock.lock();//上锁
            try {
                System.out.println(Thread.currentThread().getName()+":call");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();//最后释放
            }

        }
    }

