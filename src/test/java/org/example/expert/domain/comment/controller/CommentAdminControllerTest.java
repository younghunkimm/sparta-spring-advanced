package org.example.expert.domain.comment.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentAdminController.class)
public class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @Test
    @DisplayName("관리자 댓글 삭제 성공 테스트")
    public void deleteComment_Success() throws Exception {
        // given
        long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
            .andExpect(status().isOk());

        verify(commentAdminService, times(1)).deleteComment(commentId);
    }
}