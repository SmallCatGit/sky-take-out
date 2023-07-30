package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * TODO 自定义切面,实现公共字段自动填充
 */
@Component
@Aspect // TODO 定义为切面类
@Slf4j
public class AutoFillAspect {
    /**
     * TODO 设置切入点
     * 指定mapper包下的所有类中的所有方法,且加入了自定义的AutoFill注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    private void autoFillPointcut() {
    }

    /**
     * TODO 定义通知和切入点
     *  Before:前置通知
     *  JoinPoint:连接点
     *
     * @param joinPoint
     */
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("公共字段自动填充...");
        // 获取当前被拦截的方法对数据库操作类型
        // TODO 获取方法签名对象(反射)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // TODO 获得方法上的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 获取数据库操作类型
        OperationType operationType = autoFill.value();

        // 获取当前被拦截方法的参数--实体对象
        // 获取所有参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        // 获取实体对象(规定实体对象放参数第一位)
        Object entity = args[0];

        // 根据不同的操作类型,为对应的属性通过反射赋值
        if (operationType == OperationType.INSERT) {
            // 为4个属性赋值
            try {
                // TODO 通过反射获取get声明的方法:通过实体对象-->获取类对象-->再获取get声明的方法-->参数是方法名和参数类型
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射,为属性赋值()
                setCreateTime.invoke(entity, LocalDateTime.now());
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setCreateUser.invoke(entity, BaseContext.getCurrentId());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            // 为2个属性赋值
            try {
                // TODO 通过反射获取get声明的方法:通过实体对象-->获取类对象-->再获取get声明的方法-->参数是方法名和参数类型
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                // 通过反射,为属性赋值()
                setUpdateTime.invoke(entity, LocalDateTime.now());
                setUpdateUser.invoke(entity, BaseContext.getCurrentId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
