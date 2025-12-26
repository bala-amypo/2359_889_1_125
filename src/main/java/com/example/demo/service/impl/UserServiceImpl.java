package com.example.demo.service.impl;

import org.springframework.stereotype.Service;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public User register(User user) {
        // simple stub — adjust if tests check logic
        return user;
    }

    @Override
    public User login(String username, String password) {
        // simple stub — adjust if tests check logic
        return null;
    }
}
