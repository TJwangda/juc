[TOC]

# JUC

java操作并发的工具包![image-20210125142206545](E:\dev\picture\image-20210125142206545.png)

## 线程、进程

> java进程默认有两个线程：main线程和gc线程

+ java开启线程方式：Thread、runable、callable

+ java程序不能真正开启线程。

  ~~~java
  public synchronized void start() {
          /**
           * This method is not invoked for the main method thread or "system"
           * group threads created/set up by the VM. Any new functionality added
           * to this method in the future may have to also be added to the VM.
           *
           * A zero status value corresponds to state "NEW".
           */
          if (threadStatus != 0)
              throw new IllegalThreadStateException();
  
          /* Notify the group that this thread is about to be started
           * so that it can be added to the group's list of threads
           * and the group's unstarted count can be decremented. */
          group.add(this);
  
          boolean started = false;
          try {
              start0();
              started = true;
          } finally {
              try {
                  if (!started) {
                      group.threadStartFailed(this);
                  }
              } catch (Throwable ignore) {
                  /* do nothing. If start0 threw a Throwable then
                    it will be passed up the call stack */
              }
          }
      }
  //java调用本地方法，调用c++。java无法直接操作硬件
      private native void start0();
  ~~~

> 并发、并行

并发：多线程操作同一资源。单核cpu快速切换，形成同事执行的效果

并行：多核cpu，同事执行多条任务。

> 线程状态 6种

~~~java
public enum State {
        /**新生*/
        NEW,
        /**运行*/
        RUNNABLE,
        /**阻塞*/
        BLOCKED,
        /**等待*/
        WAITING,
        /**超时等待*/
        TIMED_WAITING,
        /**终止*/
        TERMINATED;
    }
~~~

> wait/sleep

1.wait-->Object类； sleep-->Thread类

2.wait释放锁，sleep不释放锁。

3.wait必须在同步代码块使用；sleep在哪都行。

## Lock锁

> Synchronized 本质就是队列



> Lock接口

~~~java
 Lock l = ...;
 l.lock();//加锁
 try {
   // access the resource protected by this lock
 } finally {
   l.unlock();//解锁
 }
~~~

所有已知实现类： ReentrantLock(可重入锁)、  ReentrantReadWriteLock.ReadLock（读锁） 、ReentrantReadWriteLock.WriteLock（写锁）

~~~java
	Lock lock = new ReentrantLock();
	//ReentrantLock构造函数，默认无参为非公平锁，true为公平锁
	public ReentrantLock() {
        sync = new NonfairSync();
    }
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
~~~

公平锁：先来后到。

非公平锁：可插队，后边的可以先执行。（默认）

> Synchronized和Lock的区别

1. synchronized是java内置关键字，lock是一个类
2. synchronized无法判断锁状态，lock可判断是否获取到了锁。
3. synchronized自动释放锁，lock需手动释放.
4. synchronized中一个线程获取了锁，其他线程只能等待；lock有trylock方法，等不到可以直接结束。
5. synchronized可重入，不可中断，非公平锁；lock可重入，可判断锁，可以设置是否公平锁。
6. synchronized适合锁少量代码同步问题；lock适合锁大量代码的同步问题。

> 锁是什么，怎么判断锁的是谁



## 生产者和消费者问题

> 生产者和消费者问题 Synchronized版

~~~java
/**
 * 线程之间的同步问题：生产者和消费者问题  等待唤醒、通知唤醒
 * 线程交替执行  A  B 两个操作同一个变量 num = 0
 * A num+1
 * B num-1
 */
public class A {
    public static void main(String[] args) {
        Data data = new Data();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.increment();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
    }

}

class Data{//数组 资源类
    private int number = 0;

    //+1
    public synchronized void increment() throws InterruptedException {
        if(number != 0){
            // 等待
            this.wait();
        }
        number++;
        System.out.println(Thread.currentThread().getName()+"=======>"+number);
        //通知其他线程加完了
        this.notifyAll();
    }

    //-1
    public synchronized void decrement() throws InterruptedException {
        if(number == 0){
            // 等待
            this.wait();
        }
        number--;
        System.out.println(Thread.currentThread().getName()+"=======>"+number);
        //通知其他线程减完了
        this.notifyAll();
    }
}
~~~

> 问题存在，A、B两个线程没问题，四个线程时还安全吗？ 否，存在虚假唤醒问题

虚假唤醒问题，把if改成while解决![image-20210125171627805](E:\dev\picture\image-20210125171627805.png)



> 生产者消费者问题  juc版

![image-20210125172803361](E:\dev\picture\image-20210125172803361.png)

~~~java
 class BoundedBuffer {
   final Lock lock = new ReentrantLock();
   final Condition notFull  = lock.newCondition(); //新建
   final Condition notEmpty = lock.newCondition(); 

   final Object[] items = new Object[100];
   int putptr, takeptr, count;

   public void put(Object x) throws InterruptedException {
     lock.lock(); try {
       while (count == items.length)
         notFull.await();
       items[putptr] = x;
       if (++putptr == items.length) putptr = 0;
       ++count;
       notEmpty.signal();//代替notifyall
     } finally { lock.unlock(); }
   }

   public Object take() throws InterruptedException {
     lock.lock(); try {
       while (count == 0)
         notEmpty.await();//代替wait
       Object x = items[takeptr];
       if (++takeptr == items.length) takeptr = 0;
       --count;
       notFull.signal();
       return x;
     } finally { lock.unlock(); }
   }
 } 
~~~

代码实现

~~~java
/**
 * 线程之间的同步问题：生产者和消费者问题  等待唤醒、通知唤醒
 * 线程交替执行  A  B 两个操作同一个变量 num = 0
 * A num+1
 * B num-1
 */
public class B {
    public static void main(String[] args) {
        Data2 data = new Data2();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.increment();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for(int i = 0;i<10;i++){
                try {
                    data.decrement();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
    }

}

class Data2{//数组 资源类
    private int number = 0;

    Lock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    //+1
    public void increment() throws InterruptedException {
        lock.lock();
        try {
            while (number != 0){
                // 等待
                condition.await();
            }
            number++;
            System.out.println(Thread.currentThread().getName()+"=======>"+number);
            //通知其他线程加完了
            condition.signalAll();
        } finally {
            lock.unlock();
        }

    }

    //-1
    public void decrement() throws InterruptedException {
        lock.lock();
        try {
            while (number == 0){
                // 等待
                condition.await();
            }
            number--;
            System.out.println(Thread.currentThread().getName()+"=======>"+number);
            //通知其他线程减完了
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }
}
~~~

> Conditon精准的通知和唤醒线程

~~~java
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
~~~

## 8锁现象

> 什么是锁？如何判断锁的是谁？ 对象、class

~~~java
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 2、
 */
public class Test1 {
    public static void main(String[] args) {
        Phone phone = new Phone();
        new Thread(()->{
            phone.sendSms();
        },"A").start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            phone.call();
        },"B").start();
    }

}

class Phone{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //两个方法调用的事同一个phone对象，谁先拿到谁执行。
    public synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public synchronized void call(){
        System.out.println("打电话");
    }
}
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 */
public class Test2 {
    public static void main(String[] args) {
        Phone2 phone1 = new Phone2();
        Phone2 phone2 = new Phone2();

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

class Phone2{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //两个方法调用的事同一个phone对象，谁先拿到谁执行。
    public synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public synchronized void call(){
        System.out.println("打电话");
    }

    //没有锁，不受同步影响
    public void hello(){
        System.out.println("hello==");
    }
}
/**
 * 8锁：关于锁的八个问题
 * 1、标准情况下，两个线程先执行哪个  先执行第一个线程。
 * 2、sendSms增加4秒等待，两个线程先执行哪个  先执行第一个线程。
 * 3、增加一个普通方法，先打印哪个线程结果   hello先执行
 * 4、两个对象，两个同步方法，先打印哪个线程结果   打电话先执行，两个对象不互相影响
 * 5、增加两个静态同步方法,只有一个对象，先执行哪个   发短信先执行
 * 6、增加两个静态同步方法,两个对象，先执行哪个   发短信先执行
 */
public class Test3 {
    public static void main(String[] args) {
        Phone3 phone1  = new Phone3();
        Phone3 phone2  = new Phone3();

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

class Phone3{

    //synchronized锁方法时，锁的对象是是方法的调用者
    //static 静态方法  类一加载就有了，锁class模板，phone3只有一个class对象
    public static synchronized void sendSms(){
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("发短信");
    }

    public static synchronized void call(){
        System.out.println("打电话");
    }


}
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

~~~

## 集合类不安全

> list

~~~java
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
~~~

> set

~~~java

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * .ConcurrentModificationException
 */
public class SetTest {
    public static void main(String[] args) {
//        Set<String> set = new HashSet<>();
//        Set<String> set = Collections.synchronizedSet(new HashSet<>());//方案一
        Set<String> set = new CopyOnWriteArraySet<>();//方案二

        for(int i = 1;i<=30;i++){
            new Thread(()->{
                set.add(UUID.randomUUID().toString().substring(0,5));
                System.out.println(set);
            },String.valueOf(i)).start();
        }
    }
}
~~~

HashSet的本质：

~~~java
 public HashSet() {
        map = new HashMap<>();
    }
//set的add方法本质：借用了map的key
public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
    private static final Object PRESENT = new Object(); //固定值
~~~

> Map

![image-20210201162206820](E:\dev\picture\image-20210201162206820.png)

~~~java
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
~~~



## Callable(简单)

~~~java
@FunctionalInterface
public interface Callable<V>
    
返回结果并可能引发异常的任务。 实现者定义一个没有参数的单一方法，称为call 。 
Callable接口类似于Runnable ，因为它们都是为其实例可能由另一个线程执行的类设计的。 然而，A Runnable不返回结果，也不能抛出被检查的异常。 
该Executors类包含的实用方法，从其他普通形式转换为Callable类。
1、可以有返回值 2、可以抛出异常 3、方法不同 run()/call()
~~~

~~~java
代码
~~~

















