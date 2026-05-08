package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.entity.SysRole;
import com.oracle.test.mapper.SysRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysRoleService {

    @Autowired
    private SysRoleMapper mapper;

    public List<SysRole> listAll() {
        return mapper.listAll();
    }

    @Transactional
    public Long create(SysRole role, Long updateBy) {
        role.setUpdateBy(updateBy);
        mapper.insert(role);
        return role.getRoleId();
    }

    @Transactional
    public void update(SysRole role, Long updateBy) {
        // 管理员角色（roleId=1）权限固定为全权 *，不允许通过 API 改写权限或改名为非管理员
        if (role.getRoleId() != null && role.getRoleId() == SysRole.ROLE_ADMIN) {
            role.setPermissions("*");
        }
        role.setUpdateBy(updateBy);
        mapper.update(role);
    }

    @Transactional
    public void delete(Long roleId) {
        if (roleId != null && roleId == SysRole.ROLE_ADMIN) {
            throw new BusinessException("管理员角色不可删除");
        }
        mapper.logicDelete(roleId);
    }
}

