package org.example.expert.domain.user.service;

import org.example.expert.domain.user.entity.User;

import java.util.function.Supplier;

public interface UserReader {

    User getUserOrElseThrow(long userId);

    User getUserOrElseThrow(long userId, Supplier<? extends RuntimeException> exSupplier);

}
