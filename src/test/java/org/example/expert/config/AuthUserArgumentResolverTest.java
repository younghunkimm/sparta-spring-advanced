package org.example.expert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @InjectMocks
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    private Method method;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        method = TestController.class.getMethod("testMethod", AuthUser.class, String.class);
    }

    @Test
    @DisplayName("파라미터 지원 여부 확인 성공 테스트")
    public void supportsParameter_Success() {
        // given
        MethodParameter methodParameter = new MethodParameter(method, 0);

        // when
        boolean result = authUserArgumentResolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("파라미터 지원 여부 확인 실패 테스트 - @Auth 어노테이션 없음")
    public void supportsParameter_Failure_NoAnnotation() {
        // given
        MethodParameter methodParameter = new MethodParameter(method, 1);

        // when
        boolean result = authUserArgumentResolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("파라미터 지원 여부 확인 실패 테스트 - 타입 불일치")
    public void supportsParameter_Failure_TypeMismatch() throws NoSuchMethodException {
        // given
        Method mismatchMethod = TestController.class.getMethod("mismatchMethod", String.class);
        MethodParameter mismatchParameter = new MethodParameter(mismatchMethod, 0);

        Method noAnnotationMethod = TestController.class.getMethod("noAnnotationMethod",
            AuthUser.class);
        MethodParameter noAnnotationParameter = new MethodParameter(noAnnotationMethod, 0);

        // when & then
        assertThrows(AuthException.class, () -> {
            authUserArgumentResolver.supportsParameter(mismatchParameter);
        });

        assertThrows(AuthException.class, () -> {
            authUserArgumentResolver.supportsParameter(noAnnotationParameter);
        });
    }

    @Test
    @DisplayName("파라미터 해석 성공 테스트")
    public void resolveArgument_Success() throws Exception {
        // given
        MethodParameter methodParameter = new MethodParameter(method, 0);
        Long userId = 1L;
        String email = "test@example.com";
        UserRole userRole = UserRole.USER;

        given(webRequest.getNativeRequest()).willReturn(httpServletRequest);
        given(httpServletRequest.getAttribute("userId")).willReturn(userId);
        given(httpServletRequest.getAttribute("email")).willReturn(email);
        given(httpServletRequest.getAttribute("userRole")).willReturn(userRole.toString());

        // when
        Object result = authUserArgumentResolver.resolveArgument(methodParameter, null, webRequest,
            null);

        // then
        assertThat(result).isInstanceOf(AuthUser.class);
        AuthUser authUser = (AuthUser) result;
        assertThat(authUser.getId()).isEqualTo(userId);
        assertThat(authUser.getEmail()).isEqualTo(email);
        assertThat(authUser.getUserRole()).isEqualTo(userRole);
    }

    // 테스트를 위한 더미 컨트롤러
    private static class TestController {

        public void testMethod(@Auth AuthUser authUser, String otherParam) {
        }

        public void mismatchMethod(@Auth String wrongType) {
        }

        public void noAnnotationMethod(AuthUser noAnnotation) {
        }
    }
}
