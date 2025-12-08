package ru.yandex.practicum.filmorate.aspects.LoggingAspects.storage;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@Order(40)
public class StoragesLoggingAspect {

    // Pointcut для всех методов в репозиториях
    @Pointcut("execution(* ru.yandex.practicum.filmorate.dal..*Repository.*(..))")
    public void allRepositoryMethods() {
    }

    @Before("allRepositoryMethods()")
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

    @AfterReturning(pointcut = "allRepositoryMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.debug("Успешное завершение  {} -> {}. Возвращаемое значение: {}", simpleClassName, methodName, result);
    }

    @AfterThrowing(pointcut = "allRepositoryMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.warn("Исключение в методе {} -> {}: {}", simpleClassName, methodName, ex.getMessage());
    }
}