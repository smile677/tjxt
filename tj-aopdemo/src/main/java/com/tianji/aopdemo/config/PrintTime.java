package com.tianji.aopdemo.config;

import java.lang.annotation.*;

/**
 * @author smile67
 */
// 元注解： 修饰注解的注解
// 注解的作用范围
@Target({ElementType.METHOD, ElementType.PARAMETER})
// 作用时机
@Retention(RetentionPolicy.RUNTIME)
// 可以被子类继承
@Inherited
// 生成文档
@Documented
public @interface PrintTime {
    /**
     * 模块
     */
    public String title() default "";
}
