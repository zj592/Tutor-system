package com.tutorsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.User;
import com.tutorsystem.entity.TutorResume;
import com.tutorsystem.service.TutorResumeService;
import com.tutorsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 家教简历控制器
 */
@Controller
@RequestMapping("/resume")
public class ResumeController {
    
    @Autowired
    private TutorResumeService tutorResumeService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 发布简历页面
     */
    @GetMapping("/publish")
    public String publishPage(Model model) {
        model.addAttribute("resume", new TutorResume());
        return "resume/publish";
    }
    
    /**
     * 简历详情页
     */
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        TutorResume resume = tutorResumeService.findById(id);
        if (resume == null) {
            return "error/404";
        }
        model.addAttribute("resume", resume);
        return "resume/detail";
    }
    
    /**
     * 发布简历处理
     */
    @PostMapping("/publish")
    public String publish(TutorResume resume, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);
            
            resume.setPublisherId(user.getUserId());
            boolean success = tutorResumeService.publishResume(resume);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "简历发布成功，等待管理员审核");
                return "redirect:/user/center";
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "简历发布失败，请重试");
                return "redirect:/resume/publish";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "发布失败：" + e.getMessage());
            return "redirect:/resume/publish";
        }
    }
    
    /**
     * 编辑简历页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        
        TutorResume resume = tutorResumeService.findById(id);
        if (resume == null || !resume.getPublisherId().equals(user.getUserId())) {
            return "error/403";
        }
        
        // 只允许编辑待审核状态的简历
        if (resume.getStatus() != 0) {
            return "error/403";
        }
        
        model.addAttribute("resume", resume);
        return "resume/edit";
    }
    
    /**
     * 更新简历
     */
    @PostMapping("/update")
    public String update(TutorResume resume, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);
            
            TutorResume existingResume = tutorResumeService.findById(resume.getResumeId());
            if (existingResume == null || !existingResume.getPublisherId().equals(user.getUserId())) {
                return "error/403";
            }
            
            boolean success = tutorResumeService.updateResume(resume);
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "简历更新成功");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "简历更新失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "更新失败：" + e.getMessage());
        }
        return "redirect:/user/center";
    }
    
    /**
     * 关闭简历
     */
    @PostMapping("/close/{id}")
    public String close(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        
        TutorResume resume = tutorResumeService.findById(id);
        if (resume == null || !resume.getPublisherId().equals(user.getUserId())) {
            return "error/403";
        }
        
        boolean success = tutorResumeService.closeResume(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "简历已关闭");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "操作失败");
        }
        return "redirect:/user/center";
    }



    /**
     * 浏览教员简历列表（供家长等用户查看）
     */
    @GetMapping("/list")
    public String listResumes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            IPage<TutorResume> resumePage = tutorResumeService.findPublishedResumes(page, size);
            model.addAttribute("resumePage", resumePage);
            return "resume/list";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }
}
