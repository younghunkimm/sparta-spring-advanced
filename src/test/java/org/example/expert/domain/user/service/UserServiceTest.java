package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 단건 조회 성공 테스트")
    public void getUser_Success() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse userResponse = userService.getUser(userId);

        // then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("유저 단건 조회 실패 테스트 - 유저 없음")
    public void getUser_Failure_UserNotFound() {
        // given
        long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(
            InvalidRequestException.class,
            () -> userService.getUser(userId)
        );

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    public void changePassword_Success() {
        // given
        long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String oldEncodedPassword = "encodedOldPassword";
        String newEncodedPassword = "encodedNewPassword";

        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User user = new User("test@test.com", oldEncodedPassword, UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(oldPassword, oldEncodedPassword)).willReturn(true);
        given(passwordEncoder.matches(newPassword, oldEncodedPassword)).willReturn(false);
        given(passwordEncoder.encode(newPassword)).willReturn(newEncodedPassword);

        // when
        userService.changePassword(userId, request);

        // then
        assertThat(user.getPassword()).isEqualTo(newEncodedPassword);

        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(oldPassword, oldEncodedPassword);
        verify(passwordEncoder, times(1)).matches(newPassword, oldEncodedPassword);
        verify(passwordEncoder, times(1)).encode(newPassword);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트 - 유저 없음")
    public void changePassword_Failure_UserNotFound() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword",
            "newPassword");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException invalidRequestException = assertThrows(
            InvalidRequestException.class,
            () -> userService.changePassword(userId, request)
        );

        assertThat(invalidRequestException.getMessage()).isEqualTo("User not found");

        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트 - 새 비밀번호가 기존 비밀번호와 같은 경우")
    public void changePassword_Failure_NewPasswordSameAsOld() {
        // given
        long userId = 1L;
        String oldPassword = "password";
        String newPassword = "password";
        String encodedPassword = "encodedPassword";

        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);
        User user = new User("test@test.com", encodedPassword, UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, encodedPassword)).willReturn(true);

        // when & then
        InvalidRequestException invalidRequestException = assertThrows(
            InvalidRequestException.class,
            () -> userService.changePassword(userId, request)
        );

        assertThat(invalidRequestException.getMessage()).isEqualTo("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");

        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(newPassword, encodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트 - 기존 비밀번호가 틀린 경우")
    public void changePassword_Failure_OldPasswordMismatch() {
        // given
        long userId = 1L;
        String wrongOldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        String actualOldEncodedPassword = "actualEncodedOldPassword";

        UserChangePasswordRequest request = new UserChangePasswordRequest(wrongOldPassword,
            newPassword);
        User user = new User("test@test.com", actualOldEncodedPassword, UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(newPassword, actualOldEncodedPassword)).willReturn(false);
        given(passwordEncoder.matches(wrongOldPassword, actualOldEncodedPassword)).willReturn(
            false);

        // when & then
        InvalidRequestException invalidRequestException = assertThrows(
            InvalidRequestException.class,
            () -> userService.changePassword(userId, request)
        );

        assertThat(invalidRequestException.getMessage()).isEqualTo("잘못된 비밀번호입니다.");

        verify(userRepository, times(1)).findById(anyLong());
        verify(passwordEncoder, times(1)).matches(newPassword, actualOldEncodedPassword);
        verify(passwordEncoder, times(1)).matches(wrongOldPassword, actualOldEncodedPassword);
        verify(passwordEncoder, never()).encode(anyString());
    }
}
