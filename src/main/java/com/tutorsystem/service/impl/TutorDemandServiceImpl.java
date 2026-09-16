package com.tutorsystem.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutorsystem.entity.TutorDemand;
import com.tutorsystem.entity.User;
import com.tutorsystem.mapper.TutorDemandMapper;
import com.tutorsystem.mapper.UserMapper;
import com.tutorsystem.service.TutorDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TutorDemandServiceImpl extends ServiceImpl<TutorDemandMapper, TutorDemand> implements TutorDemandService {

    @Autowired
    private TutorDemandMapper tutorDemandMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean publishDemand(TutorDemand demand) {
        demand.setPublishTime(LocalDateTime.now());
        demand.setStatus(0); // 待审核
        return tutorDemandMapper.insert(demand) > 0;
    }

    @Override
    public boolean updateDemand(TutorDemand demand) {
        demand.setStatus(0); // 编辑后重新进入待审核
        return tutorDemandMapper.updateById(demand) > 0;
    }

    /**
     * 查询需求并附带发布者信息，供详情页展示
     */
    @Override
    public TutorDemand findById(Long demandId) {
        TutorDemand demand = tutorDemandMapper.selectById(demandId);
        if (demand != null) {
            User publisher = userMapper.selectById(demand.getPublisherId());
            if (publisher != null) {
                demand.setPublisher(publisher);
            }
        }
        return demand;
    }

    @Override
    public IPage<TutorDemand> findPublishedDemands(int page, int size) {
        Page<TutorDemand> pageParam = new Page<>(page, size);
        IPage<TutorDemand> demandPage = tutorDemandMapper.selectPublishedDemandPage(pageParam);
        demandPage.getRecords().forEach(demand -> {
            User publisher = userMapper.selectById(demand.getPublisherId());
            demand.setPublisher(publisher);
        });
        return demandPage;
    }

    @Override
    public IPage<TutorDemand> searchDemands(int page, int size, String keyword) {
        Page<TutorDemand> pageParam = new Page<>(page, size);
        IPage<TutorDemand> demandPage = tutorDemandMapper.searchDemandPage(pageParam, keyword);
        demandPage.getRecords().forEach(demand -> {
            User publisher = userMapper.selectById(demand.getPublisherId());
            demand.setPublisher(publisher);
        });
        return demandPage;
    }

    @Override
    public IPage<TutorDemand> findDemandsByUser(int page, int size, Long userId) {
        Page<TutorDemand> pageParam = new Page<>(page, size);
        IPage<TutorDemand> demandPage = tutorDemandMapper.selectByPublisherId(pageParam, userId);
        // 补充发布者信息，防止前端空指针
        demandPage.getRecords().forEach(demand -> {
            User publisher = userMapper.selectById(demand.getPublisherId());
            demand.setPublisher(publisher);
        });
        return demandPage;
    }



    @Override
    public boolean closeDemand(Long demandId) {
        TutorDemand demand = tutorDemandMapper.selectById(demandId);
        if (demand != null) {
            demand.setStatus(2); // 已关闭
            return tutorDemandMapper.updateById(demand) > 0;
        }
        return false;
    }

    @Override
    public boolean deleteDemand(Long demandId) {
        return tutorDemandMapper.deleteById(demandId) > 0;
    }

    @Override
    public boolean approveDemand(Long demandId, Integer status) {
        TutorDemand demand = this.getById(demandId);
        if (demand == null) {
            return false;
        }
        demand.setStatus(status); // 1=通过（已发布）2=拒绝（置为已关闭）
        return this.updateById(demand);
    }
}