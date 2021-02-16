package com.test.juc.function;

import java.util.function.Function;

/**
 * Function 函数型接口 有一个参数，一个输出
 * 只要是函数式接口，就可以用lambda表达式简化
 */
public class Demo01 {
    public static void main(String[] args) {
//        Function<String,String> fun = new Function<String,String>(){
//
//            @Override
//            public String apply(String o) {
//                return o;
//            }
//        };
        Function<String,String> fun = (str)->{ return str; };
        System.out.println(fun.apply("asdf"));
    }
}
