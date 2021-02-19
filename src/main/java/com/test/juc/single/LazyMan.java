package com.test.juc.single;

import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * 懒汉式单例模式
 */
public class LazyMan {
    private static boolean qinjiang = false;
    private LazyMan(){
        synchronized (LazyMan.class){
            if(qinjiang == false){
                qinjiang = true;
            }else {
                throw new RuntimeException("别用反射破坏单例");
            }
        }
    }
    private static volatile LazyMan lazyMan;
//    //单线程下可以
//    public static LazyMan getInstance(){
//        if(lazyMan==null){
//            lazyMan = new LazyMan();
//        }
//        return lazyMan;
//    }
//    //多线程并发失效
//    public static void main(String[] args) {
//        for (int i = 1; i <= 10; i++) {
//            new Thread(()->{
//                LazyMan.getInstance();
//            }).start();
//        }
//    }

//    进阶
    //双重检测锁模式的懒汉式单例，检测DCL懒汉式
    public static LazyMan getInstance(){
//        加锁
        if(lazyMan==null){
            synchronized (LazyMan.class){
                if(lazyMan==null){
                    lazyMan = new LazyMan();//不是一个原子性操作
                    /**
                     * 1、分配内存空间
                     * 2、执行构造方法，初始化对象
                     * 3、把对象指向这个空间
                     *
                     * 正常步骤是123。
                     * 加入A线程执行132可以不报错。但此时如果B线程在A刚执行完步骤3时进入，此时对象实际为null，
                     * 给对象家volatile防止指令重排
                     */
                }
            }
        }
    return lazyMan;
}
    //多线程并发
//    public static void main(String[] args) {
//        for (int i = 1; i <= 10; i++) {
//            new Thread(()->{
//                LazyMan.getInstance();
//            }).start();
//        }
//    }

    //反射破坏单例
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
//        LazyMan instance = lazyMan.getInstance();
//        Constructor<LazyMan> declaredConstructor = LazyMan.class.getDeclaredConstructor(null);
//        declaredConstructor.setAccessible(true);
//        LazyMan instance  = declaredConstructor.newInstance();
//        LazyMan instance2 = declaredConstructor.newInstance();
//        System.out.println(instance);
//        System.out.println(instance2);

        Field qinjiang = LazyMan.class.getDeclaredField("qinjiang");
        qinjiang.setAccessible(true);//破坏私有

        Constructor<LazyMan> declaredConstructor = LazyMan.class.getDeclaredConstructor(null);
        declaredConstructor.setAccessible(true);
        LazyMan instance  = declaredConstructor.newInstance();

        qinjiang.set(instance,false);

        LazyMan instance2 = declaredConstructor.newInstance();
        System.out.println(instance);
        System.out.println(instance2);
    }
}
