package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("Todo 등록 성공 테스트")
    public void saveTodo_Success() {

        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        TodoSaveRequest request = new TodoSaveRequest("Test Title", "Test Contents");
        String weather = "Sunny";

        // Mockito를 사용하여 weatherClient.getWeather()가 "Sunny"를 반환하도록 설정
        given(weatherClient.getTodayWeather()).willReturn(weather);

        // Mockito를 사용하여 todoRepository.save()가 호출될 때 넘어온 인자 그대로를 반환하도록 설정
        given(todoRepository.save(any(Todo.class))).willAnswer(
            invocation -> invocation.getArgument(0));

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        // then
        // 반환된 응답(todoSaveResponse)이 null이 아닌지 확인
        assertThat(response).isNotNull();
        // 반환된 응답의 title이 요청(request)의 title과 일치하는지 확인
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        // 반환된 응답의 contents가 요청(request)의 contents와 일치하는지 확인
        assertThat(response.getContents()).isEqualTo(request.getContents());
        // 반환된 응답의 weather가 "Sunny"인지 확인
        assertThat(response.getWeather()).isEqualTo(weather);
        // 반환된 응답의 user 정보가 올바른지 확인
        assertThat(response.getUser().getId()).isEqualTo(user.getId());
        assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());

        // todoRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(todoRepository, times(1)).save(any(Todo.class));
        // weatherClient의 getTodayWeather 메서드가 한 번 호출되었는지 검증
        verify(weatherClient, times(1)).getTodayWeather();
    }

    @Test
    @DisplayName("Todo 목록 조회 성공 테스트")
    public void getTodos_Success() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        User user = new User("test@test.com", "password", UserRole.USER);
        List<Todo> todos = Arrays.asList(
            new Todo("Title1", "Contents1", "Sunny", user),
            new Todo("Title2", "Contents2", "Rainy", user)
        );
        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todoPage);

        // when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(todos.size());
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Title1");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Title2");

        verify(todoRepository, times(1)).findAllByOrderByModifiedAtDesc(pageable);
    }

    @Test
    @DisplayName("Todo 단건 조회 성공 테스트")
    public void getTodo_Success() {
        // given
        long todoId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        Todo todo = new Todo("Title1", "Contents1", "Sunny", user);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(todoId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Title1");
        assertThat(response.getContents()).isEqualTo("Contents1");
        assertThat(response.getWeather()).isEqualTo("Sunny");
        assertThat(response.getUser().getEmail()).isEqualTo("test@test.com");

        verify(todoRepository, times(1)).findByIdWithUser(todoId);
    }

    @Test
    @DisplayName("Todo 단건 조회 실패 테스트 - 존재하지 않는 Todo")
    public void getTodo_Failure_TodoNotFound() {
        // given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(todoId)).willThrow(
            new InvalidRequestException("Todo not found"));

        // when & then
        InvalidRequestException exception = assertThrows(
            InvalidRequestException.class,
            () -> todoService.getTodo(todoId)
        );
        assertEquals("Todo not found", exception.getMessage());

        verify(todoRepository, times(1)).findByIdWithUser(todoId);
    }

}
