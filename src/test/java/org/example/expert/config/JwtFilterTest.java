package org.example.expert.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    // --- Constants for Readability ---
    private static final String DEFAULT_API_URI = "/api/some-endpoint";
    private static final String ADMIN_API_URI = "/admin/some-endpoint";
    private static final String AUTH_API_URI = "/auth/login";
    // --- Helper Fields ---
    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter printWriter = new PrintWriter(stringWriter);
    // --- Mocks and Class Under Test ---
    @InjectMocks
    private JwtFilter jwtFilter;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    // --- Helper Methods for Setup ---
    private void setupToken(String token, UserRole role, String userId, String email) {
        String bearerToken = "Bearer " + token;
        Claims claims = new DefaultClaims();
        claims.setSubject(userId);
        claims.put("email", email);
        claims.put("userRole", role.name());

        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willReturn(claims);
    }

    private void setupErrorResponse() throws IOException {
        given(objectMapper.writeValueAsString(any())).willReturn("{\"message\":\"error\"}");
        given(response.getWriter()).willReturn(printWriter);
    }

    // --- Tests ---
    @Test
    @DisplayName("Auth URL은 필터를 통과해야 함")
    void doFilter_AuthUrl_ShouldPass() throws IOException, ServletException {
        // Given
        given(request.getRequestURI()).willReturn(AUTH_API_URI);

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).substringToken(anyString());
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 401 Unauthorized 반환")
    void doFilter_NoAuthorizationHeader_ShouldReturnUnauthorized()
        throws IOException, ServletException {
        // Given
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        given(request.getHeader("Authorization")).willReturn(null);
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 JWT는 요청 속성을 설정하고 통과")
    void doFilter_ValidJwt_ShouldSetAttributesAndPass() throws IOException, ServletException {
        // Given
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        setupToken("valid-token", UserRole.USER, "1", "user@example.com");

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(request).setAttribute("userId", 1L);
        verify(request).setAttribute("email", "user@example.com");
        verify(request).setAttribute("userRole", "USER");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 JWT는 401 Unauthorized 반환")
    void doFilter_ExpiredJwt_ShouldReturnUnauthorized() throws IOException, ServletException {
        // Given
        String expiredToken = "expired-token";
        String bearerToken = "Bearer " + expiredToken;
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(expiredToken);
        given(jwtUtil.extractClaims(expiredToken)).willThrow(
            new ExpiredJwtException(null, new DefaultClaims(), "Expired"));
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("잘못된 JWT는 400 Bad Request 반환")
    void doFilter_InvalidJwt_ShouldReturnBadRequest() throws IOException, ServletException {
        // Given
        String invalidToken = "invalid-token";
        String bearerToken = "Bearer " + invalidToken;
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(invalidToken);
        given(jwtUtil.extractClaims(invalidToken)).willThrow(new MalformedJwtException("Invalid"));
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("ADMIN 역할로 ADMIN URL 접근 시 통과")
    void doFilter_AdminUrl_WithAdminRole_ShouldPass() throws IOException, ServletException {
        // Given
        given(request.getRequestURI()).willReturn(ADMIN_API_URI);
        setupToken("admin-token", UserRole.ADMIN, "100", "admin@example.com");

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(request).setAttribute("userId", 100L);
        verify(request).setAttribute("email", "admin@example.com");
        verify(request).setAttribute("userRole", "ADMIN");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("USER 역할로 ADMIN URL 접근 시 403 Forbidden 반환")
    void doFilter_AdminUrl_WithUserRole_ShouldReturnForbidden()
        throws IOException, ServletException {
        // Given
        given(request.getRequestURI()).willReturn(ADMIN_API_URI);
        setupToken("user-token", UserRole.USER, "2", "user@example.com");
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Claims 추출 실패 시 401 Unauthorized 반환")
    void doFilter_ClaimsExtractionFails_ShouldReturnUnauthorized()
        throws IOException, ServletException {
        // Given
        String tokenWithNoClaims = "no-claims-token";
        String bearerToken = "Bearer " + tokenWithNoClaims;
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(tokenWithNoClaims);
        given(jwtUtil.extractClaims(tokenWithNoClaims)).willReturn(null);
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("예상치 못한 오류 발생 시 500 Internal Server Error 반환")
    void doFilter_UnexpectedError_ShouldReturnInternalServerError()
        throws IOException, ServletException {
        // Given
        String token = "some-token";
        String bearerToken = "Bearer " + token;
        given(request.getRequestURI()).willReturn(DEFAULT_API_URI);
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(token);
        given(jwtUtil.extractClaims(token)).willThrow(new RuntimeException("Unexpected error!"));
        setupErrorResponse();

        // When
        jwtFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        verify(filterChain, never()).doFilter(request, response);
    }
}