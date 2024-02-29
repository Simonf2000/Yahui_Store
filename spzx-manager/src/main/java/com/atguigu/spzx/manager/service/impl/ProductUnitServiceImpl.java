package com.atguigu.spzx.manager.service.impl;

import com.atguigu.spzx.manager.mapper.ProductUnitMapper;
import com.atguigu.spzx.manager.service.ProductUnitService;
import com.atguigu.spzx.model.entity.base.ProductUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/29/17:58
 * @Description:
 */
@Service
@Transactional
public class ProductUnitServiceImpl implements ProductUnitService {

    @Autowired
    ProductUnitMapper productUnitMapper;

    @Override
    public List<ProductUnit> findAll() {
        return  productUnitMapper.findAll();
    }
}
