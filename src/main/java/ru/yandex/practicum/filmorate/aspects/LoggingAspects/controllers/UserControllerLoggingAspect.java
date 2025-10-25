package ru.yandex.practicum.filmorate.aspects.LoggingAspects.controllers;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(20)
public class UserControllerLoggingAspect {

    // Pointcut для всех методов в контроллере UserController
    @Pointcut("execution(* ru.yandex.practicum.filmorate.controller.UserController.*(..))")
    public void userControllerMethods() {
    }

    @Before("userControllerMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов метода userController -> {} ", methodName);
        log.debug("Аргументы вызова метода {}: {}", methodName, java.util.Arrays.toString(args));
    }

    @AfterReturning(pointcut = "userControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        log.debug("Метод userController -> {} успешно завершен.", methodName);
        log.debug("Возвращаемое значение метода userController -> {}: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "userControllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();

        log.error("Исключение в методе userController -> {}: {}", methodName, ex.getMessage());
    }
}