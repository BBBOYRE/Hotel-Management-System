package com.oracle.test.mapper;

import com.oracle.test.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {

    Customer findById(Long customerId);

    Customer findByIdCard(@Param("idCard") String idCard);

    List<Customer> search(@Param("keyword") String keyword,
                          @Param("vipLevel") Integer vipLevel,
                          @Param("orderBy") String orderBy,
                          @Param("offset") int offset,
                          @Param("limit") int limit);

    long count(@Param("keyword") String keyword,
               @Param("vipLevel") Integer vipLevel);

    int insert(Customer customer);

    int update(Customer customer);

    int logicDelete(@Param("customerId") Long customerId,
                    @Param("updateBy") Long updateBy);
}
