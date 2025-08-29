package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserReadService implements UserReader {

    private final UserRepository userRepository;

    @Override
    public User getUserOrElseThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));
    }

    @Override
    public User getUserOrElseThrow(long userId, Supplier<? extends RuntimeException> exSupplier) {
        return userRepository.findById(userId)
                .orElseThrow(exSupplier);
    }
}
