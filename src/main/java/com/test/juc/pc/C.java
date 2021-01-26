package com.test.juc.pc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class C {
    public static void main(String[] args) {
        Data3 data = new Data3();
        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printA();
            }
        },"A").start();

        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printB();
            }
        },"C").start();

        new Thread(()->{
            for(int i = 0 ;i<10;i++ ){
                data.printC();
            }
        },"C").start();
    }

}

class Data3{
    private Lock lock = new ReentrantLock();

    Condition condition1 = lock.newCondition();
    Condition condition2 = lock.newCondition();
    Condition condition3 = lock.newCondition();

    private int num =1 ;//1-A执行，2-B执行，3-C执行

    public void printA(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (1 != num){
                //等待
                condition1.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>aaaaaaaaaa");
            //唤醒指定线程B
            num = 2;
            condition2.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void printB(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (2 !=num){
                condition2.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>bbbbbbbbbb");
            //唤醒指定线程C
            num = 3;
            condition3.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void printC(){
        lock.lock();
        try {//业务：判断-》执行-》通知
            while (3 !=num){
                condition3.await();
            }
            System.out.println(Thread.currentThread().getName()+"===>cccccc");
            //唤醒指定线程A
            num = 1;
            condition1.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
