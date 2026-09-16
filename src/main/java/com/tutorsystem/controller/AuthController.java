package com.tutorsystem.controller;

import com.tutorsystem.entity.User;
import com.tutorsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证控制器：登录、注册
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(value = "error", required = false) String error) {
        if (error != null) {
            model.addAttribute("errorMsg", "用户名或密码错误");
        }
        return "auth/login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /**
     * 注册处理
     */
    @PostMapping("/register")
    public String register(User user, RedirectAttributes redirectAttributes) {
        try {
            // 检查用户名是否已存在
            if (userService.findByUsername(user.getUsername()) != null) {
                redirectAttributes.addFlashAttribute("errorMsg", "用户名已存在");
                return "redirect:/register";
            }

            // 设置默认状态
            user.setStatus(1); // 正常状态

            boolean success = userService.register(user);
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "注册成功，请登录");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "注册失败，请重试");
                return "redirect:/register";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "注册失败：" + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * 登出
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }
}
