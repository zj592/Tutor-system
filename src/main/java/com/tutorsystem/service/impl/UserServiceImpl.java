package com.tutorsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutorsystem.entity.User;
import com.tutorsystem.mapper.UserMapper;
import com.tutorsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 构建权限集合
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = "";
        if (user.getRole() == 0) {
            roleName = "ROLE_PARENT";
        } else if (user.getRole() == 1) {
            roleName = "ROLE_TUTOR";
        } else if (user.getRole() == 2) {
            roleName = "ROLE_ADMIN";
        }
        authorities.add(new SimpleGrantedAuthority(roleName));
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == 1, // 是否启用
            true, // 账户是否过期
            true, // 凭证是否过期
            true, // 账户是否锁定
            authorities
        );
    }
    
    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(user.getUsername());
        if (existingUser != null) {
            return false;
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userMapper.insert(user) > 0;
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public boolean updateUser(User user) {
        return userMapper.updateById(user) > 0;
    }
    
    @Override
    public IPage<User> findAllUsers(int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        return userMapper.selectPage(pageParam, new QueryWrapper<>());
    }
    
    @Override
    public boolean toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(user.getStatus() == 1 ? 0 : 1);
            return userMapper.updateById(user) > 0;
        }
        return false;
    }
}
