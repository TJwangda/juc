package com.test.juc.lock8;

import java.util.concurrent.TimeUnit;

/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 * 5、增加两个静态同步方法,只有一个对象，先执行哪个   发短信先执行
 * 6、增加两个静态同步方法,两个对象，先执行哪个   发短信先执行
 * 7、一个静态同步方法、一个普通同步方法、一个对象，先执行哪个   先执行打电话，静态同步方法锁class对象，普通同步方法锁的调用对象，不是一个锁，不影响。
 * 8、一个静态同步方法、一个普通同步方法、两个对象，先执行哪个    先执行打电话，不同锁对象，不影响
 */
public class Test4 {
    public static void main(String[] args) {
        Phone4 phone1  = new Phone4();
        Phone4 phone2  = new Phone4();

        new Thread(()->{
            phone1.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone2.call();
        },"B").start();
    }

}

class Phone4{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //static 静态方法  类一加载就有了，锁class模板，phone4只有一个class对象
    public static synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    // 普通同步方法
    public synchronized void call(){
        System.out.println("打电话");
    }


}
