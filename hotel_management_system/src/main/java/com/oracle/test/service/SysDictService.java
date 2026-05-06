package com.oracle.test.service;

import com.oracle.test.entity.SysDict;
import com.oracle.test.mapper.SysDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDictService {

    @Autowired
    private SysDictMapper mapper;

    public List<SysDict> listAll() {
        return mapper.listAll();
    }

    public List<SysDict> listByType(String typeCode) {
        return mapper.listByType(typeCode);
    }

    public Map<String, List<SysDict>> grouped() {
        return mapper.listAll().stream().collect(Collectors.groupingBy(SysDict::getTypeCode));
    }

    @Transactional
    public Long create(SysDict dict, Long updateBy) {
        dict.setUpdateBy(updateBy);
        mapper.insert(dict);
        return dict.getDictId();
    }

    @Transactional
    public void update(SysDict dict, Long updateBy) {
        dict.setUpdateBy(updateBy);
        mapper.update(dict);
    }

    @Transactional
    public void delete(Long dictId) {
        mapper.delete(dictId);
    }
}
