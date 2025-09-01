package org.example.expert.config;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.example.expert.aop.LogAdmin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AdminApiLoggingAspectTest {

    @InjectMocks
    private AdminApiLoggingAspect adminApiLoggingAspect;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Admin API 로깅 테스트")
    @LogAdmin
    void logAdminApi() throws Throwable {
        // given
        request.setRequestURI("/api/admin/test");
        request.setAttribute("userId", 1L);
        Object[] args = {"test_request"};
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));
        when(proceedingJoinPoint.getSignature().getName()).thenReturn("testMethod");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");

        // when
        adminApiLoggingAspect.logAdminApi(proceedingJoinPoint);

        // then
        verify(proceedingJoinPoint, times(1)).proceed();
    }
}