package com.oracle.test.service;

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
        role.setUpdateBy(updateBy);
        mapper.update(role);
    }

    @Transactional
    public void delete(Long roleId) {
        mapper.logicDelete(roleId);
    }
}
