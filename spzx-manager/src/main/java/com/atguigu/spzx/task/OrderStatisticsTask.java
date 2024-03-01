package com.atguigu.spzx.task;

import cn.hutool.core.date.DateUtil;
import com.atguigu.spzx.manager.mapper.OrderInfoMapper;
import com.atguigu.spzx.manager.mapper.OrderStatisticsMapper;
import com.atguigu.spzx.model.entity.order.OrderStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/01/18:48
 * @Description:
 */
@Slf4j
@Component
public class OrderStatisticsTask {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderStatisticsMapper orderStatisticsMapper;

    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0/20 * * * * ?")
    public void orderTotalAmountStatistics() {
        String dateTime = DateUtil.offsetDay(new Date(), -1).toString(new SimpleDateFormat("yyyy-MM-dd"));
        OrderStatistics orderStatistics = orderInfoMapper.getOrderStatistics(dateTime);
        if (orderStatistics != null) {
            orderStatisticsMapper.insert(orderStatistics);
        }

    }

}
