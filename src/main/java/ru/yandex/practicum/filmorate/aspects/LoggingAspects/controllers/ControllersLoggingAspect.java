package ru.yandex.practicum.filmorate.aspects.LoggingAspects.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@Order(10)
public class ControllersLoggingAspect {

    // Pointcut для всех методов в контроллере
    @Pointcut("execution(* ru.yandex.practicum.filmorate.controller.*.*(..))")
    public void allControllerMethods() {
    }

    @Before("allControllerMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        try {
            // 1. Получаем атрибуты запроса из контекста Spring
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            // 2. Проверяем, что мы в контексте HTTP-запроса
            if (requestAttributes == null) {
                System.out.println("⚠️ Вызов вне HTTP-запроса (например, тесты или sheduled tasks)");
                return;
            }

            // 3. Приводим тип к ServletRequestAttributes (специализация для веб-запросов)
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

            // 4. Получаем объект HTTP-запроса
            HttpServletRequest request = servletRequestAttributes.getRequest();

            // 5. Извлекаем полезную информацию
            String httpMethod = request.getMethod();
            String requestURI = request.getRequestURI();
            String clientIP = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String queryString = request.getQueryString();

            // 7. Логирование запроса
            log.info("===============  Запрос {} на endpoint: {}   ========", httpMethod, requestURI);
            log.trace("=== IP: {}, userAgent: {}, queryString: {}", clientIP, userAgent, queryString);


            String controllerName = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            Object[] args = joinPoint.getArgs();

            log.info("Вызов метода {} -> {} ", controllerName, methodName);
            log.debug("args: {}", java.util.Arrays.toString(args));

        } catch (Exception e) {
            log.info("❌ Ошибка при получении информации о запросе: " + e.getMessage());
        }
    }

    @AfterReturning(pointcut = "allControllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("Успешное завершение {} -> {}. Возвращаемое значение: {}", controllerName, methodName, result);
    }

    @AfterThrowing(pointcut = "allControllerMethods()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();

        log.warn("Исключение в методе {} -> {}: {}", controllerName, methodName, ex.getMessage());
    }

}