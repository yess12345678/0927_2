package com.zstu.math.dao;

import com.zstu.math.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    Optional<LoginAttempt> findByUsername(String username);

    @Modifying
    @Query("UPDATE LoginAttempt la SET la.attemptCount = 0, la.locked = false, la.lockUntil = null WHERE la.username = :username")
    void resetLoginAttempt(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE la.lastAttemptTime < :expireTime")
    void deleteExpiredAttempts(@Param("expireTime") LocalDateTime expireTime);
}