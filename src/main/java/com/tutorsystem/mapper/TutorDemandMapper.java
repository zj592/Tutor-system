package com.tutorsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutorsystem.entity.TutorDemand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 家教需求Mapper接口
 */
@Mapper
public interface TutorDemandMapper extends BaseMapper<TutorDemand> {
    
    /**
     * 分页查询已发布的家教需求
     */
    @Select("SELECT td.*, u.username, u.phone FROM tutor_demand td " +
            "LEFT JOIN user u ON td.publisher_id = u.user_id " +
            "WHERE td.status = 1 " +
            "ORDER BY td.publish_time DESC")
    IPage<TutorDemand> selectPublishedDemandPage(Page<?> page);
    
    /**
     * 根据条件搜索已发布的家教需求
     */
    @Select("SELECT td.*, u.username, u.phone FROM tutor_demand td " +
            "LEFT JOIN user u ON td.publisher_id = u.user_id " +
            "WHERE td.status = 1 AND " +
            "(td.subject LIKE CONCAT('%', #{keyword}, '%') OR " +
            "td.grade LIKE CONCAT('%', #{keyword}, '%') OR " +
            "td.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY td.publish_time DESC")
    IPage<TutorDemand> searchDemandPage(Page<?> page, String keyword);
    
    /**
     * 根据发布者ID查询需求
     */
    @Select("SELECT * FROM tutor_demand WHERE publisher_id = #{publisherId} " +
            "ORDER BY publish_time DESC")
    IPage<TutorDemand> selectByPublisherId(Page<?> page, Long publisherId);
}
