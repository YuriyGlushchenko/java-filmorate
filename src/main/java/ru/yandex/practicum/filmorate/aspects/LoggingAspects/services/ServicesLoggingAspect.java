package ru.yandex.practicum.filmorate.aspects.LoggingAspects.services;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(30)
public class ServicesLoggingAspect {

    // Pointcut для всех методов в пакете services
    @Pointcut("execution(* ru.yandex.practicum.filmorate.service.*.*(..))")
    public void allServicesMethods() {
    }

    @Before("allServicesMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // Класс, в котором объявлен метод
        Class<?> declaringClass = signature.getDeclaringType();

        String fullClassName = declaringClass.getName();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов метода {} -> {} ", simpleClassName, methodName);
        log.debug("args: {}, класс: {}", java.util.Arrays.toString(args), fullClassName);
    }

    @AfterReturning(pointcut = "allServicesMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.debug("Метод {} -> {} успешно завершен. Возвращаемое значение: {}", simpleClassName, methodName, result);
    }

    @AfterThrowing(pointcut = "allServicesMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.warn("Исключение в методе {} -> {}: {}", simpleClassName, methodName, ex.getMessage());
    }
}