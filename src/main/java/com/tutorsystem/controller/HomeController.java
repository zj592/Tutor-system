package com.tutorsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.TutorDemand;
import com.tutorsystem.entity.TutorResume;
import com.tutorsystem.service.TutorDemandService;
import com.tutorsystem.service.TutorResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 首页控制器
 */
@Controller
public class HomeController {
    
    @Autowired
    private TutorDemandService tutorDemandService;
    
    @Autowired
    private TutorResumeService tutorResumeService;
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model, 
                       @RequestParam(defaultValue = "1") int demandPage,
                       @RequestParam(defaultValue = "1") int resumePage) {
        
        // 获取最新家教需求（前5条）
        IPage<TutorDemand> demandPageData = tutorDemandService.findPublishedDemands(demandPage, 5);
        model.addAttribute("latestDemands", demandPageData.getRecords());
        
        // 获取最新家教简历（前5条）
        IPage<TutorResume> resumePageData = tutorResumeService.findPublishedResumes(resumePage, 5);
        model.addAttribute("latestResumes", resumePageData.getRecords());
        
        return "index";
    }
    
    /**
     * 家教需求列表页
     */
    @GetMapping("/demands")
    public String demandsPage(Model model,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false) String keyword) {
        
        IPage<TutorDemand> demandPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            demandPage = tutorDemandService.searchDemands(page, 10, keyword);
            model.addAttribute("keyword", keyword);
        } else {
            demandPage = tutorDemandService.findPublishedDemands(page, 10);
        }
        
        model.addAttribute("demandPage", demandPage);
        return "demand/list";
    }
    
    /**
     * 教员简历列表页
     */
    @GetMapping("/resumes")
    public String resumesPage(Model model,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false) String keyword) {
        
        IPage<TutorResume> resumePage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            resumePage = tutorResumeService.searchResumes(page, 10, keyword);
            model.addAttribute("keyword", keyword);
        } else {
            resumePage = tutorResumeService.findPublishedResumes(page, 10);
        }
        
        model.addAttribute("resumePage", resumePage);
        return "resume/list";
    }
}
