package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class TodoReadServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoReadService todoReadService;

    @Test
    @DisplayName("Todo 단건 조회 성공 테스트")
    public void getTodoOrElseThrow_Success() {
        // given
        long todoId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        Todo todo = new Todo("title", "contents", "Sunny", user);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when
        Todo foundTodo = todoReadService.getTodoOrElseThrow(todoId);

        // then
        assertThat(foundTodo).isNotNull();
        assertThat(foundTodo.getTitle()).isEqualTo(todo.getTitle());
        assertThat(foundTodo.getContents()).isEqualTo(todo.getContents());
        assertThat(foundTodo.getWeather()).isEqualTo(todo.getWeather());
        assertThat(foundTodo.getUser()).isEqualTo(todo.getUser());

        verify(todoRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Todo 단건 조회 실패 테스트 - Todo 없음")
    public void getTodoOrElseThrow_Failure_TodoNotFound() {
        // given
        long todoId = 1L;

        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        assertThrows(
            InvalidRequestException.class,
            () -> todoReadService.getTodoOrElseThrow(todoId)
        );

        verify(todoRepository, times(1)).findById(anyLong());
    }

}
