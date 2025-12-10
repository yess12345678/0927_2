package com.zstu.math.service;

public interface LoginSecurityService {
    boolean isAccountLocked(String username);
    void recordLoginAttempt(String username, boolean success);
    void resetLoginAttempt(String username);
    void cleanupExpiredAttempts();
}