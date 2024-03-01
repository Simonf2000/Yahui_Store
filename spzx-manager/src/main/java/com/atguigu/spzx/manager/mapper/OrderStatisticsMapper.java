package com.atguigu.spzx.manager.mapper;

import com.atguigu.spzx.model.entity.order.OrderStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/01/19:03
 * @Description:
 */
@Mapper
public interface OrderStatisticsMapper {
    void insert(OrderStatistics orderStatistics);
}
