package org.example.expert.domain.comment.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    public void comment를_정상적으로_삭제한다() {
        // given
        long commentId = 1L;

        // when
        commentAdminService.deleteComment(commentId);

        // then
        verify(commentRepository, times(1)).deleteById(commentId);
    }
}