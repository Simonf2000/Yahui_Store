package com.atguigu.spzx.order.mapper;

import com.atguigu.spzx.model.entity.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    OrderInfo getById(Long orderId);

    List<OrderInfo> findUserPage(Long userId, Integer orderStatus);

    OrderInfo getByOrderNo(String orderNo);

    void updateById(OrderInfo orderInfo);
}