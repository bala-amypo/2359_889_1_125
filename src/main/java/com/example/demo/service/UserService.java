package com.example.demo.service;


import com.example.demo.entity.User;

public interface UserService {
    
    User findByEmailIgnoreCase(String email);
}