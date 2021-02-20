package com.test.juc.lock;

/**
 * 可重入锁
 * synchronized
 */
public class Demo01 {
    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(()->{
            phone.sms();
        },"a").start();

        new Thread(()->{
            phone.sms();
        },"b").start();
    }
}

class Phone{
    public synchronized void sms(){
        System.out.println(Thread.currentThread().getName()+":sms");
        call();//这里也有锁
    }

    public synchronized void call(){
        System.out.println(Thread.currentThread().getName()+":call");
    }
}
