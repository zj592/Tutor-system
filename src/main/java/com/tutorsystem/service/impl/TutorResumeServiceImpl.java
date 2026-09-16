package com.tutorsystem.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutorsystem.entity.TutorResume;
import com.tutorsystem.entity.User;
import com.tutorsystem.mapper.TutorResumeMapper;
import com.tutorsystem.mapper.UserMapper;
import com.tutorsystem.service.TutorResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TutorResumeServiceImpl extends ServiceImpl<TutorResumeMapper, TutorResume> implements TutorResumeService {

    @Autowired
    private TutorResumeMapper tutorResumeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean publishResume(TutorResume resume) {
        resume.setPublishTime(LocalDateTime.now());
        resume.setStatus(0); // 待审核
        return tutorResumeMapper.insert(resume) > 0;
    }

    @Override
    public boolean updateResume(TutorResume resume) {
        return tutorResumeMapper.updateById(resume) > 0;
    }

    @Override
    public TutorResume findById(Long resumeId) {
        TutorResume resume = tutorResumeMapper.selectById(resumeId);
        if (resume != null) {
            User publisher = userMapper.selectById(resume.getPublisherId());
            if (publisher != null) {
                resume.setPublisher(publisher);
            }
        }
        return resume;
    }

    @Override
    public IPage<TutorResume> findPublishedResumes(int page, int size) {
        Page<TutorResume> pageParam = new Page<>(page, size);
        IPage<TutorResume> resumePage = tutorResumeMapper.selectPublishedResumePage(pageParam);

        // 填充发布者信息
        resumePage.getRecords().forEach(resume -> {
            User publisher = userMapper.selectById(resume.getPublisherId());
            resume.setPublisher(publisher);
        });

        return resumePage;
    }

    @Override
    public IPage<TutorResume> searchResumes(int page, int size, String keyword) {
        Page<TutorResume> pageParam = new Page<>(page, size);
        IPage<TutorResume> resumePage = tutorResumeMapper.searchResumePage(pageParam, keyword);

        // 填充发布者信息
        resumePage.getRecords().forEach(resume -> {
            User publisher = userMapper.selectById(resume.getPublisherId());
            resume.setPublisher(publisher);
        });

        return resumePage;
    }

    @Override
    public IPage<TutorResume> findResumesByUser(int page, int size, Long userId) {
        Page<TutorResume> pageParam = new Page<>(page, size);
        return tutorResumeMapper.selectByPublisherId(pageParam, userId);
    }

    @Override
    public IPage<TutorResume> findResumesBySubject(int page, int size, String subject) {
        Page<TutorResume> pageParam = new Page<>(page, size);
        IPage<TutorResume> resumePage = tutorResumeMapper.selectBySubject(pageParam, subject);

        // 填充发布者信息
        resumePage.getRecords().forEach(resume -> {
            User publisher = userMapper.selectById(resume.getPublisherId());
            resume.setPublisher(publisher);
        });

        return resumePage;
    }



    @Override
    public boolean closeResume(Long resumeId) {
        TutorResume resume = tutorResumeMapper.selectById(resumeId);
        if (resume != null) {
            resume.setStatus(2); // 已关闭
            return tutorResumeMapper.updateById(resume) > 0;
        }
        return false;
    }

    @Override
    public boolean deleteResume(Long resumeId) {
        return tutorResumeMapper.deleteById(resumeId) > 0;
    }


    @Override
    public boolean approveResume(Long resumeId, Integer status) {
        TutorResume resume = this.getById(resumeId);
        if (resume == null) {
            return false;
        }
        resume.setStatus(status); // 1=通过（已发布）2=拒绝（置为已关闭）
        return this.updateById(resume);
    }



}