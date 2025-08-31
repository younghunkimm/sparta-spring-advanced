package org.example.expert.domain.todo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TodoController.class)
public class TodoControllerTest {

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TodoService todoService;
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
    @DisplayName("Todo 저장 성공 테스트")
    public void saveTodo_Success() throws Exception {
        // given
        TodoSaveRequest request = new TodoSaveRequest("Test Title", "Test Content");
        TodoSaveResponse response = new TodoSaveResponse(1L, "Test Title", "Test Content", "Sunny",
            new UserResponse(1L, "test@test.com"));
        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).willReturn(
            response);

        // when & then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Test Title"))
            .andExpect(jsonPath("$.contents").value("Test Content"))
            .andExpect(jsonPath("$.user.id").value(1L));
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 테스트")
    public void getTodos_Success() throws Exception {
        // given
        TodoResponse todoResponse = new TodoResponse(1L, "Test Title", "Test Content", "Sunny",
            new UserResponse(1L, "test@test.com"),
            LocalDateTime.now(), LocalDateTime.now());
        PageImpl<TodoResponse> response = new PageImpl<>(Collections.singletonList(todoResponse),
            PageRequest.of(0, 10), 1);
        given(todoService.getTodos(anyInt(), anyInt())).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].title").value("Test Title"));
    }

    @Test
    @DisplayName("Todo 단건 조회 성공 테스트")
    public void getTodo_Success() throws Exception {
        // given
        long todoId = 1L;
        TodoResponse response = new TodoResponse(1L, "Test Title", "Test Content", "Sunny",
            new UserResponse(1L, "test@test.com"), LocalDateTime.now(), LocalDateTime.now());
        given(todoService.getTodo(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos/{todoId}", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @DisplayName("Todo 저장 실패 테스트 - 잘못된 요청")
    public void saveTodo_Failure_InvalidRequest() throws Exception {
        // given
        TodoSaveRequest request = new TodoSaveRequest("", "");

        // when & then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}