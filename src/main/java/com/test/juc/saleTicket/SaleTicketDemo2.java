package com.test.juc.saleTicket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SaleTicketDemo2 {
    public static void main(String[] args) {
        //多线程操作同一资源，把资源丢入线程
        Ticket ticket = new Ticket();
        //@FunctionalInterface 函数式接口 lamda表达式(参数)->{代码}
        new Thread(()->{
            for(int i = 0;i<40;i++){
                ticket.sale();
            }
        },"A").start();
        new Thread(()->{
            for(int i = 0;i<40;i++){
                ticket.sale();
            }
        },"B").start();
        new Thread(()->{
            for(int i = 0;i<40;i++){
                ticket.sale();
            }
        },"C").start();
    }
}

//Lock锁
class Ticket2{
    private int number = 30;
    Lock lock = new ReentrantLock();
    public void sale(){
        lock.lock();//加锁
//        lock.tryLock();//尝试获取锁
        try {
            if(number > 0){
                System.out.println(Thread.currentThread().getName()+"卖出了"+(number--)+"票，剩"+number+"张");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();//解锁
        }
    }
}


