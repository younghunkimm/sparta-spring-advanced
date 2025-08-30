package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 실패 테스트 - 이미 존재하는 이메일")
    public void signup_Failure_EmailAlreadyExists() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password", "USER");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            authService.signup(request)
        );

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    public void signup_Success() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password", "USER");
        String encodedPassword = "encodedPassword";
        User user = new User(request.getEmail(), encodedPassword, UserRole.USER);
        String token = "test_token";

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(any(), anyString(), any(UserRole.class))).willReturn(token);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertEquals(token, response.getBearerToken());

        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).createToken(any(), anyString(), any(UserRole.class));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 가입되지 않은 유저")
    public void signin_Failure_UserNotFound() {
        // given
        SigninRequest request = new SigninRequest("nonexistent@test.com", "password");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            authService.signin(request)
        );

        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).createToken(any(), anyString(), any(UserRole.class));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    public void signin_Failure_InvalidPassword() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "wrong_password");
        User user = new User("test@test.com", "encoded_password", UserRole.USER);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.signin(request)
        );

        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

        verify(jwtUtil, never()).createToken(any(), anyString(), any(UserRole.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void signin_Success() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "password");
        User user = new User("test@test.com", "encoded_password", UserRole.USER);
        String token = "test_token";

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(any(), anyString(), any(UserRole.class))).willReturn(token);

        // when
        var response = authService.signin(request);

        // then
        assertEquals(token, response.getBearerToken());

        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verify(jwtUtil, times(1)).createToken(any(), anyString(), any(UserRole.class));
    }

}
