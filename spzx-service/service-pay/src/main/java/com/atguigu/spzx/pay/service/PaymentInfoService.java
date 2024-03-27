package com.atguigu.spzx.pay.service;

import com.atguigu.spzx.model.entity.pay.PaymentInfo;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/18:23
 * @Description:
 */
public interface PaymentInfoService {
    PaymentInfo savePaymentInfo(String orderNo);

    void updatePaymentStatus(Map<String, String> paramMap, Integer payType);
}