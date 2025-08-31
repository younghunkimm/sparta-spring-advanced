package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
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

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    private final AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
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
    @DisplayName("댓글 저장 성공 테스트")
    public void saveComment_Success() throws Exception {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Test Comment");
        CommentSaveResponse response = new CommentSaveResponse(1L, "Test Comment",
            new UserResponse(1L, "test@test.com"));

        given(commentService.saveComment(any(AuthUser.class), anyLong(),
            any(CommentSaveRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.contents").value("Test Comment"))
            .andExpect(jsonPath("$.user.id").value(1L))
            .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    @DisplayName("댓글 조회 성공 테스트")
    public void getComments_Success() throws Exception {
        // given
        long todoId = 1L;
        List<CommentResponse> response = Arrays.asList(
            new CommentResponse(1L, "Comment 1", new UserResponse(1L, "test1@test.com")),
            new CommentResponse(2L, "Comment 2", new UserResponse(2L, "test2@test.com"))
        );

        given(commentService.getComments(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].contents").value("Comment 1"))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[1].contents").value("Comment 2"));
    }
}
