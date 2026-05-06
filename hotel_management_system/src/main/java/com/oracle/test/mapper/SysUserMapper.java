package com.oracle.test.mapper;

import com.oracle.test.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {

    SysUser findById(Long userId);

    SysUser findByUsername(@Param("username") String username);

    List<SysUser> search(@Param("keyword") String keyword,
                         @Param("status") Integer status,
                         @Param("offset") int offset,
                         @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("status") Integer status);

    int insert(SysUser user);

    int update(SysUser user);

    int updateStatus(@Param("userId") Long userId,
                     @Param("status") Integer status,
                     @Param("updateBy") Long updateBy);

    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String password,
                       @Param("updateBy") Long updateBy);

    int logicDelete(@Param("userId") Long userId,
                    @Param("updateBy") Long updateBy);
}
