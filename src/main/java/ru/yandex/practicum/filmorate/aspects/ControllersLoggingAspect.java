package ru.yandex.practicum.filmorate.aspects;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
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

            // 6. Формируем полный URL
            StringBuffer requestURL = request.getRequestURL();
            String queryString = request.getQueryString();
            String fullURL = queryString == null ?
                    requestURL.toString() :
                    requestURL.append('?').append(queryString).toString();

            // 7. Логирование
            System.out.println("=== ДЕТАЛИ ЗАПРОСА ===");
            System.out.println("Метод: " + httpMethod);
            System.out.println("URL: " + fullURL);
            System.out.println("URI: " + requestURI);
            System.out.println("IP клиента: " + clientIP);
            System.out.println("User-Agent: " + userAgent);
            System.out.println("Метод контроллера: " + joinPoint.getSignature().getName());

        } catch (Exception e) {
            System.out.println("❌ Ошибка при получении информации о запросе: " + e.getMessage());
        }
    }

}