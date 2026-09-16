package com.tutorsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.User;
import com.tutorsystem.entity.TutorDemand;
import com.tutorsystem.entity.TutorResume;
import com.tutorsystem.service.UserService;
import com.tutorsystem.service.TutorDemandService;
import com.tutorsystem.service.TutorResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 用户中心控制器
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TutorDemandService tutorDemandService;

    @Autowired
    private TutorResumeService tutorResumeService;

    /**
     * 个人中心首页 - 正确保留，无需修改
     */
    @GetMapping("/center")
    public String userCenter(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);

        // 根据角色显示不同内容（家长看需求，教员看简历）
        if (user.getRole() == 0) { // 家长
            IPage<TutorDemand> demandPage = tutorDemandService.findDemandsByUser(1, 10, user.getUserId());
            model.addAttribute("myDemands", demandPage.getRecords());
        } else if (user.getRole() == 1) { // 教员
            IPage<TutorResume> resumePage = tutorResumeService.findResumesByUser(1, 10, user.getUserId());
            model.addAttribute("myResumes", resumePage.getRecords());
        }

        return "user/center";
    }

    /**
     * 进入编辑个人信息页面  ✔ 路径：/user/edit  正确匹配前端跳转链接
     */
    @GetMapping("/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "user/edit";
    }

    /**
     * 提交更新个人信息  ✔ 路径：/user/update  正确匹配前端表单提交地址
     * ✔ 你的这个方法逻辑非常好！只更新允许修改的手机号/邮箱，防止篡改角色/ID等敏感信息，必须保留
     */
    @PostMapping("/update")
    public String updateProfile(User user, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userService.findByUsername(username);

            // 仅允许修改邮箱和手机号
            currentUser.setEmail(user.getEmail());
            currentUser.setPhone(user.getPhone());

            boolean success = userService.updateUser(currentUser);
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "个人信息更新成功");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "个人信息更新失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "更新失败：" + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/user/center";
    }

}