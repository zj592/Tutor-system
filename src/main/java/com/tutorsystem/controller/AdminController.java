package com.tutorsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.TutorDemand;
import com.tutorsystem.entity.TutorResume;
import com.tutorsystem.entity.User;
import com.tutorsystem.service.TutorDemandService;
import com.tutorsystem.service.TutorResumeService;
import com.tutorsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 管理员后台：需求审核、简历审核、用户管理
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TutorDemandService tutorDemandService;

    @Autowired
    private TutorResumeService tutorResumeService;

    @Autowired
    private UserService userService;

    // 管理员首页
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // 待审核需求列表
    @GetMapping("/demands/review")
    public String demandReview(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            Model model) {
        try {
            // 查询【待审核】的需求 status=0
            IPage<TutorDemand> demandPage = tutorDemandService.lambdaQuery()
                    .eq(TutorDemand::getStatus, 0)
                    .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size));
            model.addAttribute("demandPage", demandPage);
        } catch (Exception e) {
            model.addAttribute("errorMsg", "加载待审核需求失败：" + e.getMessage());
            e.printStackTrace();
        }
        return "admin/demand-review";
    }

    // 需求审核操作（通过/拒绝）
    @PostMapping("/demands/approve/{id}")
    public String approveDemand(@PathVariable Long id,
                                @RequestParam Integer status,
                                RedirectAttributes redirectAttributes) {
        boolean success = tutorDemandService.approveDemand(id, status);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", status == 1 ? "需求审核通过！" : "需求审核拒绝！");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "审核操作失败，请重试！");
        }
        return "redirect:/admin/demands/review";
    }

    // 待审核简历列表
    @GetMapping("/resumes/review")
    public String resumeReview(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            Model model) {
        try {
            // 查询【待审核】的简历 status=0
            IPage<TutorResume> resumePage = tutorResumeService.lambdaQuery()
                    .eq(TutorResume::getStatus, 0)
                    .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size));
            model.addAttribute("resumePage", resumePage);
        } catch (Exception e) {
            model.addAttribute("errorMsg", "加载待审核简历失败：" + e.getMessage());
            e.printStackTrace();
        }
        return "admin/resume-review";
    }

    // 简历审核操作（通过/拒绝）
    @PostMapping("/resumes/approve/{id}")
    public String approveResume(@PathVariable Long id,
                                @RequestParam Integer status,
                                RedirectAttributes redirectAttributes) {
        boolean success = tutorResumeService.approveResume(id, status);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", status == 1 ? "简历审核通过！" : "简历审核拒绝！");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "审核操作失败，请重试！");
        }
        return "redirect:/admin/resumes/review";
    }

    // 用户列表
    @GetMapping("/users")
    public String userList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            Model model) {
        try {
            // 查询所有用户（分页）
            IPage<User> userPage = userService.findAllUsers(page, size);
            model.addAttribute("userPage", userPage);
        } catch (Exception e) {
            model.addAttribute("errorMsg", "加载用户列表失败：" + e.getMessage());
            e.printStackTrace();
        }
        return "admin/user-list";
    }

    // 用户禁用/启用切换
    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean success = userService.toggleUserStatus(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "用户状态切换成功！");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "状态切换失败，请重试！");
        }
        return "redirect:/admin/users";
    }
}