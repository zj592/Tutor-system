package com.tutorsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家教简历实体类
 */
@Data
@TableName("tutor_resume")
public class TutorResume {
    
    @TableId(value = "resume_id", type = IdType.AUTO)
    private Long resumeId;
    
    @TableField("publisher_id")
    private Long publisherId;
    
    @TableField("subjects")
    private String subjects;
    
    @TableField("experience")
    private String experience;
    
    @TableField("available_time")
    private String availableTime;
    
    @TableField("expected_salary")
    private BigDecimal expectedSalary;
    
    @TableField("introduction")
    private String introduction;
    
    @TableField("certificates")
    private String certificates;
    
    @TableField("publish_time")
    private LocalDateTime publishTime;
    
    /**
     * 状态：0-待审核，1-已发布，2-已关闭
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 发布者信息（非数据库字段）
     */
    @TableField(exist = false)
    private User publisher;
    
    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 0:
                return "待审核";
            case 1:
                return "已发布";
            case 2:
                return "已关闭";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取可教科目数组（用于前端显示）
     */
    public String[] getSubjectArray() {
        if (subjects == null || subjects.isEmpty()) {
            return new String[0];
        }
        return subjects.split("，|,");
    }
}
