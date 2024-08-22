package com.tianji.aopdemo.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Date;

@Component
// 表示切面类
@Aspect
public class AopConfig {

    /*// 定义切点
    // 不灵活
//    @Pointcut("execution( public * com.tianji.aopdemo.service.*.get*(..))")
//    public void pt() {
//    }
    // 灵活
    // 切的是注解
    @Pointcut("@annotation(com.tianji.aopdemo.config.PrintTime)")
    public void pt() {
    }

    // 环绕通知@Around 前置 @Before 后置 @After 异常 @AfterThrowing 最终 @AfterReturning
    @Around("pt()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("开始时间new Date() = " + new Date());
        Object[] args = joinPoint.getArgs();
        // 执行目标方法
        Object proceed = joinPoint.proceed(args);

        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            PrintTime methodAnnotation = method.getAnnotation(PrintTime.class);
            if (methodAnnotation != null) {
                // 打印注解的属性值
                System.out.println("methodAnnotation.value() = " + methodAnnotation.title());
            }
        }
        System.out.println("结束时间：new Date() = " + new Date());
        return proceed;
    }*/


    // 环绕通知@Around 前置 @Before 后置 @After 异常 @AfterThrowing 最终 @AfterReturning
    @Around("@annotation(printTime)")
    public Object around(ProceedingJoinPoint joinPoint, PrintTime printTime) throws Throwable {
        System.out.println("开始时间new Date() = " + new Date());
        Object[] args = joinPoint.getArgs();
        // 执行目标方法
        Object proceed = joinPoint.proceed(args);

        System.out.println(printTime.title());
        System.out.println("结束时间：new Date() = " + new Date());
        return proceed;
    }
}
