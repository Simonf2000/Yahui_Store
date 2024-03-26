package com.atguigu.spzx.order.service;

import com.atguigu.spzx.model.dto.h5.OrderInfoDto;
import com.atguigu.spzx.model.entity.order.OrderInfo;
import com.atguigu.spzx.model.vo.h5.TradeVo;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/9:09
 * @Description:
 */
public interface OrderInfoService {
    public TradeVo getTrade();

    Long submitOrder(OrderInfoDto orderInfoDto);

    OrderInfo getOrderInfo(Long orderId);

    TradeVo buy(Long skuId);
}
