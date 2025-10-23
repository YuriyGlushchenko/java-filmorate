package ru.yandex.practicum.filmorate.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class FilmControllerLoggingAspect {

    // Pointcut для всех методов в контроллере
    @Pointcut("execution(* ru.yandex.practicum.filmorate.controller.FilmController.*(..))")
    public void filmControllerMethods() {
    }

    @Before("filmControllerMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов метода filmController -> {} с аргументами: {}", methodName, java.util.Arrays.toString(args));
    }

    @AfterReturning(pointcut = "filmControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();

        log.info("Метод {} успешно завершен. Результат: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "filmControllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();

        log.error("Исключение в методе filmController -> {}: {}", methodName, ex.getMessage());
    }
}