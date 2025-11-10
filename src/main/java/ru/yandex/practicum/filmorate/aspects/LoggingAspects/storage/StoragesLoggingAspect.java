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

    // Pointcut для всех методов в пакете storage
    @Pointcut("execution(* ru.yandex.practicum.filmorate.dal..*(..))")
    public void allStorageMethods() {
    }

    @Before("allStorageMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // Класс, в котором объявлен метод
        Class<?> declaringClass = signature.getDeclaringType();

        String fullClassName = declaringClass.getName();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов метода {} -> {} ", simpleClassName, methodName);
        log.debug("Класс: {}, args: {}", fullClassName, java.util.Arrays.toString(args));
    }

    @AfterReturning(pointcut = "allStorageMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.debug("Метод {} -> {} успешно завершен. Возвращаемое значение: {}", simpleClassName, methodName, result);
    }

    @AfterThrowing(pointcut = "allStorageMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<?> declaringClass = signature.getDeclaringType();
        String simpleClassName = declaringClass.getSimpleName();

        String methodName = signature.getName();

        log.warn("Исключение в методе {} -> {}: {}", simpleClassName, methodName, ex.getMessage());
    }
}