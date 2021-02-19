package com.test.juc.single;

import java.lang.reflect.Constructor;

/**
 * enum since jdk1.5,本事是一个class
 */
public enum EnumSingle {
    INSTANCE;
    public EnumSingle getInstance(){
        return INSTANCE;
    }
}

class Test{
    public static void main(String[] args) throws  Exception {
//        EnumSingle instance2 = EnumSingle.INSTANCE;
        EnumSingle instance1 = EnumSingle.INSTANCE;
        System.out.println(instance1);

        //尝试反射
//        Constructor<EnumSingle> declaredConstructor = EnumSingle.class.getDeclaredConstructor(null);
        Constructor<EnumSingle> declaredConstructor = EnumSingle.class.getDeclaredConstructor(String.class,int.class);
        declaredConstructor.setAccessible(true);

        EnumSingle instance2 = declaredConstructor.newInstance();
        System.out.println(instance2);
    }

}