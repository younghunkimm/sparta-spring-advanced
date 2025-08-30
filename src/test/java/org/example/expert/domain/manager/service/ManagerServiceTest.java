package org.example.expert.domain.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.service.TodoReader;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserReader userReader;
    @Mock
    private TodoReader todoReader;

    @InjectMocks
    private ManagerService managerService;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoReader.getTodoOrElseThrow(todoId)).willThrow(
            new InvalidRequestException("Todo not found"));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

        assertEquals("일정에 담당자가 없습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(),
            managerResponses.get(0).getUser().getEmail());
    }

    @Test
        // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(
            managerUserId); // request dto 생성

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);
        given(userReader.getUserOrElseThrow(eq(managerUserId),
            ArgumentMatchers.<Supplier<? extends RuntimeException>>any())).willReturn(managerUser);
        given(managerRepository.save(any(Manager.class))).willAnswer(
            invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId,
            managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    public void todo를_등록할_때_일정을_만든_유저가_아니라면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // 요청하는 유저
        User todoOwner = new User("owner@owner.com", "password", UserRole.USER); // 일정을 소유한 유저
        ReflectionTestUtils.setField(todoOwner, "id", 2L);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", todoOwner);

        long managerUserId = 3L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

        assertEquals("일정을 생성한 유저만 담당자를 지정할 수 있습니다.", exception.getMessage());
    }

    @Test
    public void todo를_등록할_때_매니저로_등록하려는_유저가_존재하지_않는다면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long nonExistentUserId = 99L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(nonExistentUserId);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);

        /*
        <문제>
        ## willThrow 대신 willAnswer를 사용하여 람다 표현식을 실행하도록 변경
        1. 기존의 given(...).willThrow(...) 구문은 userReader.getUserOrElseThrow 메서드가 호출될 때, 설정된 InvalidRequestException을 즉시 발생
        2. 이로 인해 ManagerService의 saveManager 메서드 안에 있는 람다 표현식이 실제로 실행되지 않음

        <이유>
        1. 테스트 코드에서 given(...).willThrow(...) 구문은 userReader.getUserOrElseThrow 메서드가 호출될 때, 메서드의 실제 코드를 실행하는 대신 설정된
            InvalidRequestException을 즉시 발생시키도록 합니다.
        2. 따라서 ManagerService의 saveManager 메서드 안에 있는 람다 표현식 () -> new InvalidRequestException(...)은 userReader.getUserOrElseThrow 메서드에
            인자로 전달되기는 하지만, 실제로 실행되지는 않습니다.
        3. 코드 커버리지 도구(예: JaCoCo)는 이 람다 표현식 내부의 코드가 실행되지 않았다는 것을 감지하고, 해당 라인이 완전히 커버되지 않았다고 표시합니다.

        <해결>
        1. 이를 해결하기 위해, 테스트 코드에서 userReader.getUserOrElseThrow 메서드가 호출될 때, 설정된 예외를 즉시 발생시키는 대신, 람다 표현식을 실행하도록 변경할 수 있습니다.
        2. 이렇게 하면, 람다 표현식 내부의 코드가 실제로 실행되어 코드 커버리지 도구에 의해 해당 라인이 커버된 것으로 인식됩니다.
        3. 즉, 람다 표현식이 호출될 때 예외를 던지도록 설정하여, 코드 커버리지를 높일 수 있습니다.
         */
        given(userReader.getUserOrElseThrow(
            eq(nonExistentUserId),
            ArgumentMatchers.<Supplier<? extends RuntimeException>>any()
        )).willAnswer(invocation -> {
            Supplier<? extends RuntimeException> exceptionSupplier = invocation.getArgument(1);
            throw exceptionSupplier.get();
        });

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

        assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    public void todo를_등록할_때_일정_작성자가_본인을_매니저로_등록하려고_한다면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 1L; // The same user
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);
        given(userReader.getUserOrElseThrow(eq(managerUserId),
            ArgumentMatchers.<Supplier<? extends RuntimeException>>any())).willReturn(user);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
            () -> managerService.saveManager(authUser, todoId, managerSaveRequest));

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 매니저_삭제_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 99L; // Non-existent todo
        long managerId = 1L;

        given(todoReader.getTodoOrElseThrow(todoId)).willThrow(
            new InvalidRequestException("Todo not found"));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void 매니저_삭제_시_일정을_만든_유저가_아니라면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User todoOwner = new User("owner@owner.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(todoOwner, "id", 2L);

        long todoId = 1L;
        long managerId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", todoOwner);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    public void 매니저_삭제_시_Manager가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        long todoId = 1L;
        long managerId = 99L; // Non-existent manager
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    public void 매니저_삭제_시_일정에_등록된_매니저가_아니라면_InvalidRequestException_에러를_던진다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        long todoId1 = 1L;
        long todoId2 = 2L;
        long managerId = 1L;

        Todo todo1 = new Todo("Test Title 1", "Test Contents 1", "Sunny", user);
        ReflectionTestUtils.setField(todo1, "id", todoId1);
        Todo todo2 = new Todo("Test Title 2", "Test Contents 2", "Cloudy", user);
        ReflectionTestUtils.setField(todo2, "id", todoId2);

        User managerUser = new User("manager@manager.com", "password", UserRole.USER);
        Manager manager = new Manager(managerUser, todo2); // Manager of todo2

        given(todoReader.getTodoOrElseThrow(todoId1)).willReturn(todo1);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.deleteManager(authUser, todoId1, managerId)
        );

        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }

    @Test
    public void 매니저_삭제에_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        ReflectionTestUtils.setField(user, "id", 1L);

        long todoId = 1L;
        long managerId = 1L;

        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        User managerUser = new User("manager@manager.com", "password", UserRole.USER);
        Manager manager = new Manager(managerUser, todo);

        given(todoReader.getTodoOrElseThrow(todoId)).willReturn(todo);
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        managerService.deleteManager(authUser, todoId, managerId);

        // then
        verify(managerRepository, times(1)).delete(manager);
    }
}
