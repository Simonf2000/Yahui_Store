package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.atguigu.spzx.manager.mapper.OrderStatisticsMapper;
import com.atguigu.spzx.manager.service.OrderStatisticsService;
import com.atguigu.spzx.model.dto.order.OrderStatisticsDto;
import com.atguigu.spzx.model.entity.order.OrderStatistics;
import com.atguigu.spzx.model.vo.order.OrderStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/01/20:22
 * @Description:
 */
@Service
@Transactional
public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    @Autowired
    OrderStatisticsMapper orderStatisticsMapper;

    @Override
    public OrderStatisticsVo getOrderStatisticsData(OrderStatisticsDto orderStatisticsDto) {

        if (!StringUtils.hasText(orderStatisticsDto.getCreateTimeBegin()) && !StringUtils.hasText(orderStatisticsDto.getCreateTimeEnd())) {
            orderStatisticsDto.setCreateTimeBegin(DateUtil.offsetDay(new Date(),-7).toString("yyyy-MM-dd"));
            orderStatisticsDto.setCreateTimeEnd(DateUtil.offsetDay(new Date(),-1).toString("yyyy-MM-dd"));
        }

        List<OrderStatistics> orderStatisticsList = orderStatisticsMapper.getOrderStatisticsData(orderStatisticsDto);

        List<String> dateList = orderStatisticsList.stream()
                .map(orderStatistics -> DateUtil.format(orderStatistics.getOrderDate(), "yyyy-MM-dd"))
                .collect(Collectors.toList());
        List<BigDecimal> amountList = orderStatisticsList.stream().map(OrderStatistics::getTotalAmount).collect(Collectors.toList());

        OrderStatisticsVo orderStatisticsVo = new OrderStatisticsVo();
        orderStatisticsVo.setDateList(dateList);
        orderStatisticsVo.setAmountList(amountList);
        return orderStatisticsVo;
    }
}
