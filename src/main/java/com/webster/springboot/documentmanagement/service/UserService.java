package com.webster.springboot.documentmanagement.service;

import com.webster.springboot.documentmanagement.dao.SessionRepository;
import com.webster.springboot.documentmanagement.dao.UserRepository;
import com.webster.springboot.documentmanagement.entity.User;
import com.webster.springboot.documentmanagement.entity.UserSession;
import com.webster.springboot.documentmanagement.model.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
//@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserService {
    private final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity registerUser(User newUser) {
        try {
            User user = userRepository.findByEmail(newUser.getEmail());
            if (user != null) {
                System.out.println("User already exists with this email:" + user.getEmail());
                UserSession session = UserSession.of(user.getId(), UUID.randomUUID().toString());

                sessionRepository.save(session);

                Map<String, Object> payload = new HashMap<>();
                payload.put("user", user);
                payload.put("session", session);


                return ResponseEntity.status(HttpStatus.OK).body(payload);
            }
            newUser.setId(0L);
            String encodedPassword = passwordEncoder.encode(newUser.getPassword());
            newUser.setPassword(encodedPassword);

            User savedUser = userRepository.save(newUser);
            System.out.println("User Registered with Id: " + savedUser.getId());

            //TODO register Login page (Response same as login API)
            UserSession session = UserSession.of(newUser.getId(), UUID.randomUUID().toString());

            sessionRepository.save(session);

            Map<String, Object> payload = new HashMap<>();
            payload.put("user", newUser);
            payload.put("session", session);

            return ResponseEntity.status(HttpStatus.OK).body(payload);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Something went wrong, try again.");

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity loginUser(LoginRequest request) {


        User user = userRepository.findByEmail(request.getEmail());

        System.out.println("user:" + user);

        if (user != null) {

            boolean res = passwordEncoder.matches(request.getPassword(), user.getPassword());

            if (res) {

                UserSession session = UserSession.of(user.getId(), UUID.randomUUID().toString());

                sessionRepository.save(session);

                Map<String, Object> payload = new HashMap<>();
                payload.put("user", user);
                payload.put("session", session);

                return ResponseEntity.status(HttpStatus.OK).body(payload);

//                httpSession = request.getSession();
//                httpSession.setAttribute("user", user);
//                request.getSession().setAttribute("user", user);


//                return "user logged in successfully\n" + user.getId();
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("incorrect password, kindly try again!");
//            return "incorrect password, kindly try again!";

        } else {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error logging in!");
//            return "error logging in!";


        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity logoutUser(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            //TODO requesyrinedId(401)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No User session present!");
        }
        //TODO UserSession --> Get sesion -> isactive ->true active false --> updateat update --> false error(401)

        UserSession session = sessionRepository.findBySessionId(sessionId);
        if (session.isActive()) {
            session.setActive(false);
            session.setUpdatedAt(new Date());
            return ResponseEntity.status(HttpStatus.OK).body("user logged out " + session.getUserId());
        }

//        httpSession = request.getSession();
//        User user = (User) httpSession.getAttribute("user");
//        httpSession.invalidate();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("error in logging out the user!" + session.getUserId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity logoutAllUserSessions(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            //TODO requesyrinedId(401)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No User session present!");
        }

        UserSession session = sessionRepository.findBySessionId(sessionId);
        Long userId = session.getUserId();

        List<UserSession> activeUserSessions = sessionRepository.findByUserIdAndActive(userId, true);

        for (UserSession usersession : activeUserSessions) {
            usersession.setActive(false);
            usersession.setUpdatedAt(new Date());
        }

        return ResponseEntity.status(HttpStatus.OK).body("all user sessions logged out for user : " + userId);
    }


//    @Transactional
//    public void save(User user) {
//        String encodedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encodedPassword);
//        userRepository.save(user);
//    }
//
//    @Transactional
//    public List<User> findAll() {
//        List<User> usersList = userRepository.findAll();
//        return usersList;
//    }
//
//    @Transactional
//    public User findUserByEmail(String email) {
//        User users = userRepository.findByEmail(email);
//        return users;
//    }
//
//    @Transactional
//    public User findByEmailAndPassword(String email, String password) {
//
//        passwordEncoder.matches(password, )
//        User user = userRepository.findByEmailAndPassword(email, password);
//        return user;
//    }
}
