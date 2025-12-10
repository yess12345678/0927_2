package com.zstu.math.service.impl;

import com.zstu.math.dao.LoginAttemptRepository;
import com.zstu.math.entity.LoginAttempt;
import com.zstu.math.service.LoginSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginSecurityServiceImpl implements LoginSecurityService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Override
    public boolean isAccountLocked(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsername(username);
        if (attemptOpt.isPresent()) {
            LoginAttempt attempt = attemptOpt.get();
            if (attempt.getLocked() && attempt.getLockUntil() != null) {
                boolean stillLocked = LocalDateTime.now().isBefore(attempt.getLockUntil());
                if (!stillLocked) {
                    // 锁定期已过，自动解锁
                    attempt.setLocked(false);
                    attempt.setAttemptCount(0);
                    loginAttemptRepository.save(attempt);
                }
                return stillLocked;
            }
        }
        return false;
    }

    @Override
    public void recordLoginAttempt(String username, boolean success) {
        LoginAttempt attempt = loginAttemptRepository.findByUsername(username)
                .orElse(new LoginAttempt(username));

        attempt.setLastAttemptTime(LocalDateTime.now());

        if (success) {
            attempt.setAttemptCount(0);
            attempt.setLocked(false);
            attempt.setLockUntil(null);
        } else {
            attempt.setAttemptCount(attempt.getAttemptCount() + 1);
            if (attempt.getAttemptCount() >= 3) {
                attempt.setLocked(true);
                attempt.setLockUntil(LocalDateTime.now().plusMinutes(1));
            }
        }

        loginAttemptRepository.save(attempt);
    }

    @Override
    public void resetLoginAttempt(String username) {
        loginAttemptRepository.resetLoginAttempt(username);
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟清理一次过期记录
    public void cleanupExpiredAttempts() {
        LocalDateTime expireTime = LocalDateTime.now().minusHours(1);
        loginAttemptRepository.deleteExpiredAttempts(expireTime);
    }
}