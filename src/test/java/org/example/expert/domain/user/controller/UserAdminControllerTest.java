package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AdminApiLoggingAspect;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAdminController.class)
@Import(AdminApiLoggingAspect.class)
@EnableAspectJAutoProxy
public class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAdminService userAdminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 권한 변경 성공 테스트")
    public void changeUserRole_Success() throws Exception {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");
        doNothing().when(userAdminService)
            .changeUserRole(anyLong(), any(UserRoleChangeRequest.class));

        // when & then
        mockMvc.perform(patch("/admin/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}