package com.tutorsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tutorsystem.entity.TutorResume;

public interface TutorResumeService extends IService<TutorResume> {

    /**
     * 发布家教简历
     */
    boolean publishResume(TutorResume resume);
    
    /**
     * 更新家教简历
     */
    boolean updateResume(TutorResume resume);
    
    /**
     * 根据ID查询简历
     */
    TutorResume findById(Long resumeId);
    
    /**
     * 分页查询已发布的简历
     */
    IPage<TutorResume> findPublishedResumes(int page, int size);
    
    /**
     * 搜索简历
     */
    IPage<TutorResume> searchResumes(int page, int size, String keyword);
    
    /**
     * 查询用户发布的简历
     */
    IPage<TutorResume> findResumesByUser(int page, int size, Long userId);
    
    /**
     * 根据科目查询简历
     */
    IPage<TutorResume> findResumesBySubject(int page, int size, String subject);
    
    /**
     * 审核简历
     */
    boolean approveResume(Long resumeId, Integer status);
    
    /**
     * 关闭简历
     */
    boolean closeResume(Long resumeId);
    
    /**
     * 删除简历
     */
    boolean deleteResume(Long resumeId);


}
