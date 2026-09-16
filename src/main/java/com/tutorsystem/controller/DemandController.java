package com.tutorsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.User;
import com.tutorsystem.entity.TutorDemand;
import com.tutorsystem.service.TutorDemandService;
import com.tutorsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 家教需求：发布、编辑、关闭、列表与详情
 */
@Controller
@RequestMapping("/demand")
public class DemandController {

    @Autowired
    private TutorDemandService tutorDemandService;

    @Autowired
    private UserService userService;

    /**
     * 发布需求页面
     */
    @GetMapping("/publish")
    public String publishPage(Model model) {
        model.addAttribute("demand", new TutorDemand());
        return "demand/publish";
    }

    /**
     * 需求详情页
     */
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable Long id, Model model) {
        TutorDemand demand = tutorDemandService.findById(id);
        if (demand == null) {
            model.addAttribute("msg", "该家教需求不存在或已删除！");
            return "error/404";
        }
        model.addAttribute("demand", demand);
        return "demand/detail";
    }

    /**
     * 发布需求处理
     */
    @PostMapping("/publish")
    public String publish(TutorDemand demand, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);

            demand.setPublisherId(user.getUserId());
            boolean success = tutorDemandService.publishDemand(demand);

            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "需求发布成功，等待管理员审核");
                return "redirect:/user/center";
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "需求发布失败，请重试");
                return "redirect:/demand/publish";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "发布失败：" + e.getMessage());
            return "redirect:/demand/publish";
        }
    }

    /**
     * 编辑需求页面
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        TutorDemand demand = tutorDemandService.findById(id);
        // 只能编辑自己发布的需求
        if (demand == null || !demand.getPublisherId().equals(user.getUserId())) {
            return "error/403";
        }

        // 只允许编辑待审核状态的需求
        if (demand.getStatus() != 0) {
            return "error/403";
        }

        model.addAttribute("demand", demand);
        return "demand/edit";
    }

    /**
     * 更新需求
     */
    @PostMapping("/update")
    public String update(TutorDemand demand, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUsername(username);

            TutorDemand existingDemand = tutorDemandService.findById(demand.getDemandId());
            if (existingDemand == null || !existingDemand.getPublisherId().equals(user.getUserId())) {
                return "error/403";
            }

            boolean success = tutorDemandService.updateDemand(demand);
            if (success) {
                redirectAttributes.addFlashAttribute("successMsg", "需求更新成功，等待管理员审核");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "需求更新失败");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "更新失败：" + e.getMessage());
        }
        return "redirect:/user/center";
    }

    /**
     * 关闭需求
     */
    @PostMapping("/close/{id}")
    public String close(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);

        TutorDemand demand = tutorDemandService.findById(id);
        if (demand == null || !demand.getPublisherId().equals(user.getUserId())) {
            return "error/403";
        }

        boolean success = tutorDemandService.closeDemand(id);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "需求已关闭");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "操作失败");
        }
        return "redirect:/user/center";
    }

    /**
     * 需求列表（分页 + 关键词搜索）
     */
    @GetMapping("/list")
    public String demandList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        try {
            IPage<TutorDemand> demandPage;
            if (keyword != null && !keyword.trim().isEmpty()) {
                demandPage = tutorDemandService.searchDemands(page, size, keyword.trim());
            } else {
                demandPage = tutorDemandService.findPublishedDemands(page, size);
            }
            model.addAttribute("demandPage", demandPage);
            model.addAttribute("keyword", keyword);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "加载家教需求列表失败，请稍后重试！");
        }
        return "demand/list";
    }

    /**
     * 我发布的需求
     */
    @GetMapping("/my")
    public String myDemand(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "8") Integer size,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User loginUser = userService.findByUsername(username);

        IPage<TutorDemand> demandPage = tutorDemandService.findDemandsByUser(page, size, loginUser.getUserId());
        model.addAttribute("demandPage", demandPage);
        return "demand/my";
    }

}