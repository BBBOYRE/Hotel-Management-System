package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.SysRole;
import com.oracle.test.entity.SysUser;
import com.oracle.test.mapper.SysRoleMapper;
import com.oracle.test.mapper.SysUserMapper;
import com.oracle.test.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysUserService {

    @Autowired private SysUserMapper userMapper;
    @Autowired private SysRoleMapper roleMapper;

    public PageResult<SysUser> page(String keyword, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<SysUser> records = userMapper.search(keyword, status, offset, pageSize);
        long total = userMapper.count(keyword, status);
        records.forEach(u -> u.setPassword(null));
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public SysUser get(Long userId) {
        SysUser user = userMapper.findById(userId);
        if (user != null) user.setPassword(null);
        return user;
    }

    public List<SysRole> roles() {
        return roleMapper.listAll();
    }

    @Transactional
    public Long create(SysUser user, String rawPassword, Long updateBy) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.encryptPassword(rawPassword);
        user.setUpdateBy(updateBy);
        if (user.getStatus() == null) user.setStatus(1);
        userMapper.insert(user);
        return user.getUserId();
    }

    @Transactional
    public void update(SysUser user, Long updateBy) {
        SysUser db = userMapper.findById(user.getUserId());
        if (db == null) throw new BusinessException("用户不存在");
        db.setRealName(user.getRealName());
        db.setRoleId(user.getRoleId());
        db.setStatus(user.getStatus() == null ? db.getStatus() : user.getStatus());
        db.setUpdateBy(updateBy);
        userMapper.update(db);
    }

    @Transactional
    public void switchStatus(Long userId, Integer status, Long updateBy) {
        userMapper.updateStatus(userId, status, updateBy);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword, Long updateBy) {
        if (!PasswordUtil.isStrong(newPassword)) {
            throw new BusinessException("密码强度不足：至少 6 位且包含字母与数字");
        }
        userMapper.updatePassword(userId, PasswordUtil.encrypt(newPassword), updateBy);
    }

    @Transactional
    public void delete(Long userId, Long updateBy) {
        if (userId.equals(updateBy)) {
            throw new BusinessException("不能删除自己");
        }
        userMapper.logicDelete(userId, updateBy);
    }

    @Transactional
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        SysUser db = userMapper.findById(userId);
        if (db == null) throw new BusinessException("用户不存在");
        if (!db.verifyPassword(oldPwd)) {
            throw new BusinessException("原密码错误");
        }
        if (!PasswordUtil.isStrong(newPwd)) {
            throw new BusinessException("新密码强度不足");
        }
        userMapper.updatePassword(userId, PasswordUtil.encrypt(newPwd), userId);
    }
}
