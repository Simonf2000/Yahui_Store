package com.atguigu.spzx.order.mapper;

import com.atguigu.spzx.model.entity.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/11:36
 * @Description:
 */
@Mapper
public interface OrderInfoMapper {
    void save(OrderInfo orderInfo);
}