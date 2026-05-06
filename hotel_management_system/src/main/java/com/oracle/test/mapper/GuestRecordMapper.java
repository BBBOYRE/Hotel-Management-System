package com.oracle.test.mapper;

import com.oracle.test.entity.GuestRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GuestRecordMapper {

    List<GuestRecord> listByRecord(@Param("recordId") Long recordId);

    int insert(GuestRecord guest);

    int delete(@Param("guestId") Long guestId);
}
