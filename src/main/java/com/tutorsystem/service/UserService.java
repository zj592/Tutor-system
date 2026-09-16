package com.tutorsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tutorsystem.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {


    
    /**
     * 用户注册
     */
    boolean register(User user);
    
    /**
     * 根据用户名查询用户
     */
    User findByUsername(String username);
    
    /**
     * 根据用户ID查询用户
     */
    User findById(Long userId);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
    
    /**
     * 分页查询所有用户
     */
    IPage<User> findAllUsers(int page, int size);
    
    /**
     * 禁用/启用用户
     */
    boolean toggleUserStatus(Long userId);
}
