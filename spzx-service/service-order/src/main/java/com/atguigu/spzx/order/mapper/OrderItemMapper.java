package com.atguigu.spzx.order.mapper;

import com.atguigu.spzx.model.entity.order.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/11:37
 * @Description:
 */
@Mapper
public interface OrderItemMapper {
    void save(OrderItem orderItem);

    List<OrderItem> findByOrderId(Long id);
}