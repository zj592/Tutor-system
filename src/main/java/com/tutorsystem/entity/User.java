package com.tutorsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("phone")
    private String phone;
    
    /**
     * 角色：0-家长，1-教员，2-管理员
     */
    @TableField("role")
    private Integer role;
    
    /**
     * 状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 角色名称（非数据库字段）
     */
    @TableField(exist = false)
    private String roleName;
    
    /**
     * 获取角色名称
     */
    public String getRoleName() {
        if (role == null) return "";
        switch (role) {
            case 0:
                return "家长";
            case 1:
                return "教员";
            case 2:
                return "管理员";
            default:
                return "未知";
        }
    }
    
    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return "";
        return status == 1 ? "正常" : "禁用";
    }
}
