package com.tutorsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tutorsystem.entity.TutorDemand;

public interface TutorDemandService extends IService<TutorDemand> {


    
    /**
     * 发布家教需求
     */
    boolean publishDemand(TutorDemand demand);
    
    /**
     * 更新家教需求
     */
    boolean updateDemand(TutorDemand demand);
    
    /**
     * 根据ID查询需求
     */
    TutorDemand findById(Long demandId);
    
    /**
     * 分页查询已发布的需求
     */
    IPage<TutorDemand> findPublishedDemands(int page, int size);
    
    /**
     * 搜索需求
     */
    IPage<TutorDemand> searchDemands(int page, int size, String keyword);
    
    /**
     * 查询用户发布的需求
     */
    IPage<TutorDemand> findDemandsByUser(int page, int size, Long userId);
    
    /**
     * 审核需求
     */
    boolean approveDemand(Long demandId, Integer status);
    
    /**
     * 关闭需求
     */
    boolean closeDemand(Long demandId);
    
    /**
     * 删除需求
     */
    boolean deleteDemand(Long demandId);


}
