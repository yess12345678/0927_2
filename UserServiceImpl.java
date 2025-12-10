package com.zstu.math.service.impl;

import com.zstu.math.dao.UserDao;
import com.zstu.math.dao.LoginAttemptRepository;
import com.zstu.math.entity.User;
import com.zstu.math.entity.LoginAttempt;
import com.zstu.math.service.IUserService;
import com.zstu.math.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService, CommandLineRunner {

    @Autowired
    private UserDao userDao;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 在应用启动时自动创建默认管理员用户
    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUser();
    }

    private void createDefaultAdminUser() {
        if (!userDao.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setNickname("系统管理员");
            admin.setLoginAttempts(0);
            admin.setAccountLocked(false);
            userDao.save(admin);
            System.out.println("==========================================");
            System.out.println("自动创建默认管理员用户成功！");
            System.out.println("用户名: admin");
            System.out.println("密码: 123456");
            System.out.println("昵称: 系统管理员");
            System.out.println("==========================================");
        } else {
            System.out.println("默认管理员用户已存在");
        }
    }

    @Override
    public boolean validateUser(String username, String password) {
        return validateUserByLoginId(username, password);
    }

    @Override
    public boolean validateUserByLoginId(String loginId, String password) {
        // 检查登录尝试限制
        if (isAccountLocked(loginId)) {
            return false;
        }

        Optional<User> userOpt = userDao.findByUsernameOrNickname(loginId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (passwordEncoder.matches(password, user.getPassword())) {
                // 登录成功
                recordLoginAttempt(loginId, true);
                user.setLoginAttempts(0);
                user.setAccountLocked(false);
                user.setLockUntil(null);
                user.setLastLoginTime(LocalDateTime.now());
                userDao.save(user);
                return true;
            } else {
                // 登录失败
                recordLoginAttempt(loginId, false);
                user.setLoginAttempts(user.getLoginAttempts() + 1);
                if (user.getLoginAttempts() >= 3) {
                    user.setAccountLocked(true);
                    user.setLockUntil(LocalDateTime.now().plusMinutes(1));
                }
                userDao.save(user);
                return false;
            }
        }

        // 用户不存在也记录失败尝试
        recordLoginAttempt(loginId, false);
        return false;
    }

    // 检查账户是否被锁定
    private boolean isAccountLocked(String loginId) {
        Optional<User> userOpt = userDao.findByUsernameOrNickname(loginId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getAccountLocked()) && user.getLockUntil() != null) {
                boolean stillLocked = LocalDateTime.now().isBefore(user.getLockUntil());
                if (!stillLocked) {
                    // 锁定期已过，自动解锁
                    user.setAccountLocked(false);
                    user.setLoginAttempts(0);
                    user.setLockUntil(null);
                    userDao.save(user);
                }
                return stillLocked;
            }
        }
        return false;
    }

    // 记录登录尝试
    private void recordLoginAttempt(String loginId, boolean success) {
        LoginAttempt attempt = loginAttemptRepository.findByUsername(loginId)
                .orElse(new LoginAttempt(loginId));

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
    public int getLoginAttempts(String username) {
        Optional<User> userOpt = userDao.findByUsername(username);
        return userOpt.map(User::getLoginAttempts).orElse(0);
    }

    @Override
    public boolean usernameExists(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    public boolean nicknameExists(String nickname) {
        return userDao.existsByNickname(nickname);
    }

    @Override
    public User addUser(User user) {
        if (usernameExists(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (nicknameExists(user.getNickname())) {
            throw new RuntimeException("昵称已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setLoginAttempts(0);
        user.setAccountLocked(false);
        user.setCreatedTime(LocalDateTime.now());

        return userDao.save(user);
    }

    @Override
    public User registerUser(User user) {
        // registerUser 和 addUser 功能相同，可以调用 addUser
        return addUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getUserByLoginId(String loginId) {
        return userDao.findByUsernameOrNickname(loginId);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.getUsername().equals(userDetails.getUsername()) &&
                usernameExists(userDetails.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (!user.getNickname().equals(userDetails.getNickname()) &&
                nicknameExists(userDetails.getNickname())) {
            throw new RuntimeException("昵称已存在");
        }

        user.setUsername(userDetails.getUsername());
        user.setNickname(userDetails.getNickname());

        // 只有在密码不为空时才更新密码
        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userDao.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    @Override
    public boolean unlockUser(Long userId) {
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAccountLocked(false);
            user.setLoginAttempts(0);
            user.setLockUntil(null);
            userDao.save(user);

            // 同时清理登录安全记录
            loginAttemptRepository.resetLoginAttempt(user.getUsername());
            return true;
        }
        return false;
    }
}