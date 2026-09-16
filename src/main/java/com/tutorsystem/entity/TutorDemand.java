package com.tutorsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家教需求实体类
 */
@Data
@TableName("tutor_demand")
public class TutorDemand {

    @TableId(value = "demand_id", type = IdType.AUTO)
    private Long demandId;

    @TableField("publisher_id")
    private Long publisherId;

    @TableField("subject")
    private String subject;

    @TableField("grade")
    private String grade;

    @TableField("description")
    private String description;

    @TableField("salary")
    private BigDecimal salary;

    @TableField("contact")
    private String contact;

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
     * 发布时间格式化为 yyyy-MM-dd HH:mm
     */
    public String getFormatPublishTime() {
        if (publishTime == null) return "";
        return publishTime.toString().replace("T", " ").substring(0, 16);
    }
}

