package com.webster.springboot.documentmanagement.controller;

import com.webster.springboot.documentmanagement.entity.User;
import com.webster.springboot.documentmanagement.model.LoginRequest;
import com.webster.springboot.documentmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity registerUser(@RequestBody User newUser) {
        ResponseEntity result = userService.registerUser(newUser);
        return result;
    }

    @PostMapping("/login")
    public ResponseEntity loginUser(@RequestBody LoginRequest request) {
        ResponseEntity result = userService.loginUser(request);
        return result;
    }

    @PostMapping("/logout")
    public ResponseEntity logoutUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity result = userService.logoutUser(sessionId);
        return result;
    }

    //TODO LOGOUT ALL Session
    @PostMapping("/logoutAllSessions")
    public ResponseEntity logoutAllUserSessions(@RequestHeader(HttpHeaders.AUTHORIZATION) String sessionId) {
        ResponseEntity result = userService.logoutAllUserSessions(sessionId);
        return result;
    }

}
