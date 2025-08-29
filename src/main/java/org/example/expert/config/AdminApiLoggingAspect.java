package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminApiLoggingAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(org.example.expert.aop.LogAdmin)")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest httpRequest = getHttpRequest();

        if (httpRequest == null) {
            // request 또는 response 값이 null 이면 종료
            // @Around는 메서드 실행을 감싸는 래퍼이므로 proceed()를 호출해야 원래 메서드가 실행됨
            log.warn("HttpServletRequest 객체를 가져올 수 없음");
            return joinPoint.proceed();
        }

        // 메서드 실행 전 요청 데이터 로깅
        String methodName = joinPoint.getSignature().getName();

        Long userId = (Long) httpRequest.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now();
        String requestURI = httpRequest.getRequestURI();
        String requestBody = getRequestBody(joinPoint);

        log.info("[Admin API Logging]");
        log.info(
                "REQUEST INFO: methodName = {}, userId = {}, requestTime = {}, requestURI = {}, requestBody = {}",
                methodName,
                userId,
                requestTime,
                requestURI,
                requestBody
        );

        // 타겟 메서드 실행
        Object result;
        try {
            result = joinPoint.proceed(); // 실제 API 실행
        } catch (Exception e) {
            log.error(
                    "ERROR: methodName = {}, requestURI = {}, message = {}",
                    methodName,
                    requestURI,
                    e.getMessage()
            );

            throw e; // 처리는 RestControllerAdvice 에게 위임
        }

        // 메서드 실행 후 응답 데이터 로깅
        String responseBody = convertObjectToJson(result);

        log.info(
                "RESPONSE INFO: methodName = {}, requestURI = {}, responseBody = {}",
                methodName,
                requestURI,
                responseBody
        );

        return result;
    }

    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest() : null;
    }

    private String getRequestBody(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args.length > 0) {
            try {
                return Arrays.stream(args)
                        .map(this::convertObjectToJson)
                        .reduce((arg1, arg2) -> arg1 + ", " + arg2)
                        .orElse("");
            } catch (Exception e) {
                log.error("Error serializing request body", e);
            }
        }

        return "";
    }

    private String convertObjectToJson(Object object) {
        if (object == null) return "";

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON", e);
            return "Error serializing object to JSON";
        }
    }
}
