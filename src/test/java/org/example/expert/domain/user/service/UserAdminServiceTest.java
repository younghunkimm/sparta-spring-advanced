package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
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
public class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    @DisplayName("유저 권한 변경 성공 테스트")
    public void changeUserRole_Success() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, request);

        // then
        assertThat(user.getUserRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("유저 권한 변경 실패 테스트 - 유저 없음")
    public void changeUserRole_Failure_UserNotFound() {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> {
            userAdminService.changeUserRole(userId, request);
        });
    }
}
