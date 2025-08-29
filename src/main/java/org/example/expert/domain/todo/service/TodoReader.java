package org.example.expert.domain.todo.service;

import org.example.expert.domain.todo.entity.Todo;

public interface TodoReader {

    Todo getTodoOrElseThrow(long todoId);

}
