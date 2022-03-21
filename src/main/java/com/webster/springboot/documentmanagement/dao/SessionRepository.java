package com.webster.springboot.documentmanagement.dao;

import com.webster.springboot.documentmanagement.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<UserSession, Long> {
    
    UserSession findBySessionIdAndActive(String sessionId, boolean active);

    List<UserSession> findByUserIdAndActive(Long userId, boolean active);

    UserSession findBySessionId(String sessionId);
}
