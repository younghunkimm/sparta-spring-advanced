package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.function.Supplier;
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
public class UserReadServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserReadService userReadService;

    @Test
    @DisplayName("유저 단건 조회 성공 테스트")
    public void getUserOrElseThrow_Success() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userReadService.getUserOrElseThrow(userId);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(foundUser.getUserRole()).isEqualTo(user.getUserRole());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("유저 단건 조회 실패 테스트 - 유저 없음")
    public void getUserOrElseThrow_Failure_UserNotFound() {
        // given
        long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(
            InvalidRequestException.class,
            () -> userReadService.getUserOrElseThrow(userId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found");

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("유저 단건 조회 성공 테스트 (Supplier)")
    public void getUserOrElseThrowWithSupplier_Success() {
        // given
        long userId = 1L;
        User user = new User("test", "password", UserRole.USER);
        Supplier<InvalidRequestException> exSupplier =
            () -> new InvalidRequestException("Custom Message");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User foundUser = userReadService.getUserOrElseThrow(userId, exSupplier);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(foundUser.getUserRole()).isEqualTo(user.getUserRole());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("유저 단건 조회 실패 테스트 - 유저 없음 (Supplier)")
    public void getUserOrElseThrowWithSupplier_Failure_UserNotFound() {
        // given
        long userId = 1L;
        Supplier<InvalidRequestException> exSupplier =
            () -> new InvalidRequestException("Custom Message");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(
            InvalidRequestException.class,
            () -> userReadService.getUserOrElseThrow(userId, exSupplier)
        );

        assertThat(exception.getMessage()).isEqualTo("Custom Message");

        verify(userRepository, times(1)).findById(anyLong());
    }

}
