package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(User user) {

        if (userRepository.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        // Default values
        if (user.getRole() == null) {
            user.setRole("USER");
        }

        user.setActive(true);

        return userRepository.save(user);
    }

    @Override
    public User findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElse(null);
    }
}
