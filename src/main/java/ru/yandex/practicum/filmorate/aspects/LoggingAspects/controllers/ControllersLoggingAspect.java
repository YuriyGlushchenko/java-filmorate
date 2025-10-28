package ru.yandex.practicum.filmorate.aspects.LoggingAspects.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
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
    public void logRequestDetails(JoinPoint joinPoint) {
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

            // 7. Логирование
            log.info("===== Запрос {} на {} -> метод контроллера: {} =====", httpMethod, requestURI, joinPoint.getSignature().getName());
            log.trace("=== IP: {}, userAgent: {}, queryString: {}", clientIP, userAgent, queryString);
        } catch (Exception e) {
            log.info("❌ Ошибка при получении информации о запросе: " + e.getMessage());
        }
    }

}