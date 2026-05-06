package com.oracle.test.mapper;

import com.oracle.test.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDictMapper {

    List<SysDict> listAll();

    List<SysDict> listByType(@Param("typeCode") String typeCode);

    SysDict findById(Long dictId);

    int insert(SysDict dict);

    int update(SysDict dict);

    int delete(Long dictId);
}
