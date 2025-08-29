package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoReadService implements TodoReader {

    private final TodoRepository todoRepository;

    @Override
    public Todo getTodoOrElseThrow(long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
    }
}
