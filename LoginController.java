package com.zstu.math.controller;

import com.zstu.math.entity.User;
import com.zstu.math.service.IUserService;
import com.zstu.math.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {
    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private boolean isNotAuthenticated(HttpSession session) {
        return session.getAttribute("currentUser") == null;
    }

    @GetMapping("/")
    public String showLoginPage(Model model, HttpSession session) {
        String loginId = (String) session.getAttribute("lastLoginId");
        if (loginId != null && !loginId.isEmpty()) {
            model.addAttribute("loginAttempts", userService.getLoginAttempts(loginId));
            model.addAttribute("lastLoginId", loginId);
        }
        return "auth/login";
    }

    // 显示注册页面
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    // 用户注册
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String nickname,
                           HttpSession session,
                           Model model) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 会在service中加密
            user.setNickname(nickname);

            User savedUser = userService.registerUser(user);
            model.addAttribute("success", "注册成功，请登录");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("error", "注册失败: " + e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("nickname", nickname);
            return "auth/register";
        }
    }

    // 支持用户名或昵称登录
    @PostMapping("/login")
    public String login(@RequestParam String loginId,
                        @RequestParam String password,
                        HttpSession session, Model model) {

        if (loginId.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "登录ID和密码不能为空");
            return "auth/login";
        }

        if (userService.validateUserByLoginId(loginId, password)) {
            // 获取用户信息（显示昵称）
            User user = userService.getUserByLoginId(loginId).orElse(null);
            if (user != null) {
                session.setAttribute("currentUser", user.getNickname()); // 显示昵称
                session.setAttribute("currentUsername", user.getUsername()); // 保存用户名用于业务逻辑
            }
            session.removeAttribute("lastLoginId");
            return "redirect:/home";
        } else {
            session.setAttribute("lastLoginId", loginId);
            int attempts = userService.getLoginAttempts(loginId);
            model.addAttribute("error", String.format("登录ID或密码错误（剩余尝试次数：%d）", 3 - attempts));
            model.addAttribute("lastLoginId", loginId);
            return "auth/login";
        }
    }

    @GetMapping("/dashboard")
    public String redirectToHome() {
        return "redirect:/home";
    }

    @GetMapping("/users")
    public String showUserManagement(HttpSession session, Model model) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("currentUser", session.getAttribute("currentUser"));
            return "system/users";
        } catch (Exception e) {
            model.addAttribute("error", "加载用户列表失败：" + e.getMessage());
            model.addAttribute("currentUser", session.getAttribute("currentUser"));
            return "system/users";
        }
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, Model model, HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                throw new RuntimeException("用户名不能为空");
            }
            if (user.getNickname() == null || user.getNickname().isEmpty()) {
                throw new RuntimeException("昵称不能为空");
            }
            userService.addUser(user);
            return "redirect:/users?success=添加用户成功";
        } catch (RuntimeException e) {
            addUserManagementAttributes(model, session, e.getMessage());
            return "system/users";
        }
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        try {
            userService.updateUser(id, user);
            return "redirect:/users?success=更新用户成功";
        } catch (Exception e) {
            return "redirect:/users?error=更新失败：" + e.getMessage();
        }
    }

    @GetMapping("/users/unlock/{id}")
    public String unlockUser(@PathVariable Long id, HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        userService.unlockUser(id);
        return "redirect:/users?success=用户已解锁";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (isNotAuthenticated(session)) return "redirect:/";

        User userToDelete = userService.getUserById(id).orElse(null);
        String currentUsername = (String) session.getAttribute("currentUsername");
        if (userToDelete != null && userToDelete.getUsername().equals(currentUsername)) {
            return "redirect:/users?error=不能删除当前登录用户";
        }
        userService.deleteUser(id);
        return "redirect:/users?success=用户已删除";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private void addUserManagementAttributes(Model model, HttpSession session, String error) {
        model.addAttribute("error", error);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("currentUser", session.getAttribute("currentUser"));
    }
}