package org.example.expert.domain.user.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserRoleTest {

    @ParameterizedTest
    @CsvSource({"USER,USER", "ADMIN,ADMIN"})
    @DisplayName("of 메서드 성공 테스트 - 유효한 역할")
    void of_Success(String input, UserRole expected) {
        // when
        UserRole result = UserRole.of(input);

        // then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("of 메서드 실패 테스트 - 유효하지 않은 역할")
    void of_Failure_InvalidRole() {
        // given
        String invalidRole = "INVALID_ROLE";

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            UserRole.of(invalidRole)
        );

        assertEquals("유효하지 않은 UerRole", exception.getMessage());
    }
}