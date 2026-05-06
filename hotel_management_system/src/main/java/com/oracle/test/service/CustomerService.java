package com.oracle.test.service;

import com.oracle.test.common.BusinessException;
import com.oracle.test.common.PageResult;
import com.oracle.test.entity.Customer;
import com.oracle.test.mapper.CustomerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerMapper mapper;

    public PageResult<Customer> page(String keyword, Integer vipLevel, String orderBy,
                                     int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Customer> records = mapper.search(keyword, vipLevel, orderBy, offset, pageSize);
        long total = mapper.count(keyword, vipLevel);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    public Customer get(Long id) {
        return mapper.findById(id);
    }

    public Customer findByIdCard(String idCard) {
        return mapper.findByIdCard(idCard);
    }

    @Transactional
    public Long create(Customer customer, Long updateBy) {
        customer.validate();
        if (mapper.findByIdCard(customer.getIdCard()) != null) {
            throw new BusinessException("该身份证号已存在客户档案");
        }
        customer.setUpdateBy(updateBy);
        mapper.insert(customer);
        return customer.getCustomerId();
    }

    @Transactional
    public void update(Customer customer, Long updateBy) {
        Customer db = mapper.findById(customer.getCustomerId());
        if (db == null) throw new BusinessException("客户不存在");
        db.setCustName(customer.getCustName());
        db.setPhone(customer.getPhone());
        db.setGender(customer.getGender());
        db.setVipLevel(customer.getVipLevel());
        db.setIdCard(db.getIdCard());
        db.validate();
        db.setUpdateBy(updateBy);
        mapper.update(db);
    }

    @Transactional
    public void delete(Long customerId, Long updateBy) {
        mapper.logicDelete(customerId, updateBy);
    }
}
