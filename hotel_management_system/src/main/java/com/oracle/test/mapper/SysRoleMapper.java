package com.oracle.test.mapper;

import com.oracle.test.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    List<SysRole> listAll();

    SysRole findById(Long roleId);

    int insert(SysRole role);

    int update(SysRole role);

    int logicDelete(Long roleId);
}
