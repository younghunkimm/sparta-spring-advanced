package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.user.validation.annotation.ValidPassword;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserChangePasswordRequest {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @ValidPassword
    private String newPassword;
}
