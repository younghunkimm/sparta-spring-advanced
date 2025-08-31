package org.example.expert.domain.manager.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ManagerController.class)
public class ManagerControllerTest {

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ManagerService managerService;
    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() throws Exception {
        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).willReturn(
            authUser);
    }

    @Test
    @DisplayName("담당자 저장 성공 테스트")
    public void saveManager_Success() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L,
            new UserResponse(2L, "test@test.com"));
        given(managerService.saveManager(any(AuthUser.class), anyLong(),
            any(ManagerSaveRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.user.id").value(2L));
    }

    @Test
    @DisplayName("담당자 목록 조회 성공 테스트")
    public void getManagers_Success() throws Exception {
        // given
        long todoId = 1L;
        List<ManagerResponse> response = Collections.singletonList(
            new ManagerResponse(2L, new UserResponse(2L, "test@test.com")));
        given(managerService.getManagers(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].user.id").value(2L))
            .andExpect(jsonPath("$[0].user.email").value("test@test.com"));
    }

    @Test
    @DisplayName("담당자 삭제 성공 테스트")
    public void deleteManager_Success() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 1L;
        doNothing().when(managerService).deleteManager(any(AuthUser.class), anyLong(), anyLong());

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId))
            .andExpect(status().isOk());
    }
}