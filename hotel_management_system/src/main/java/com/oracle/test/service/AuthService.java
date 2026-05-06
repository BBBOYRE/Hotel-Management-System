package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.LoginUser;
import com.oracle.test.entity.SysUser;
import com.oracle.test.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private SysUserMapper userMapper;

    public LoginUser login(String username, String rawPassword) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!user.isEnabled()) {
            throw new BusinessException("账号已被禁用");
        }
        if (!user.verifyPassword(rawPassword)) {
            throw new BusinessException("用户名或密码错误");
        }
        return new LoginUser(user.getUserId(), user.getUsername(),
                user.getRealName(), user.getRoleId(), user.getRoleName());
    }
}
