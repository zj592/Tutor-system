package com.tutorsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorsystem.entity.TutorResume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 家教简历Mapper接口
 */
@Mapper
public interface TutorResumeMapper extends BaseMapper<TutorResume> {


    /**
     * 分页查询已发布的家教简历
     */
    @Select("SELECT tr.*, u.username, u.phone FROM tutor_resume tr " +
            "LEFT JOIN user u ON tr.publisher_id = u.user_id " +
            "WHERE tr.status = 1 " +
            "ORDER BY tr.publish_time DESC")
    IPage<TutorResume> selectPublishedResumePage(Page<?> page);

    /**
     * 根据条件搜索已发布的家教简历
     */
    @Select("SELECT tr.*, u.username, u.phone FROM tutor_resume tr " +
            "LEFT JOIN user u ON tr.publisher_id = u.user_id " +
            "WHERE tr.status = 1 AND " +
            "(tr.subjects LIKE CONCAT('%', #{keyword}, '%') OR " +
            "tr.experience LIKE CONCAT('%', #{keyword}, '%') OR " +
            "tr.introduction LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY tr.publish_time DESC")
    IPage<TutorResume> searchResumePage(Page<?> page, String keyword);

    /**
     * 根据发布者ID查询简历
     */
    @Select("SELECT * FROM tutor_resume WHERE publisher_id = #{publisherId} " +
            "ORDER BY publish_time DESC")
    IPage<TutorResume> selectByPublisherId(Page<?> page, Long publisherId);

    /**
     * 根据科目查询已发布的简历
     */
    @Select("SELECT tr.*, u.username, u.phone FROM tutor_resume tr " +
            "LEFT JOIN user u ON tr.publisher_id = u.user_id " +
            "WHERE tr.status = 1 AND tr.subjects LIKE CONCAT('%', #{subject}, '%') " +
            "ORDER BY tr.publish_time DESC")
    IPage<TutorResume> selectBySubject(Page<?> page, String subject);
}
