package com.atguigu.spzx.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.pay.properties.AlipayProperties;
import com.atguigu.spzx.pay.service.AlipayService;
import com.atguigu.spzx.pay.service.PaymentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/26/19:02
 * @Description:
 */
@Controller
@RequestMapping("/api/order/alipay")
@Slf4j
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    @Autowired
    AlipayProperties alipayProperties;


    @Autowired
    PaymentInfoService paymentInfoService;

    @Operation(summary="支付宝下单")
    @GetMapping("/submitAlipay/{orderNo}")
    @ResponseBody
    public Result<String> submitAlipay(@Parameter(name = "orderNo", description = "订单号", required = true)
                                       @PathVariable(value = "orderNo") String orderNo) throws Exception {
        //返回结果字符串，是一个form表单。这个表单提交就会打开手机端支付宝app,然后弹出支付密码框。用户输入密码就可以支付付款了。
        //支付平返回的表单。
        String form = alipayService.submitAlipay(orderNo);
        return Result.build(form, ResultCodeEnum.SUCCESS);
    }

    //异步通知接口： 支付宝扣款后，支付宝服务器会异步调用尚品甄选支付微服务接口，通知已扣款完成。如果没收到通知，24小时，发7次通知。
    //4m 10m 10m 1h 2h 6h 15h
    @RequestMapping("/callback/notify")
    @ResponseBody
    public String alipayNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) {

        log.info("AlipayController...alipayNotify方法执行了..."); //临时打印信息，测试是否回调成功。TODO 其他业务稍后完成

        //验签：支付宝扣款后异步回调，会回传一些数据，验证这些数据有效性。（例如：必填参数，数据是否被篡改等）
        //支付宝平台使用私钥加密，我们用公钥解密，来验签。
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(paramMap, alipayProperties.getAlipayPublicKey(), AlipayProperties.charset, AlipayProperties.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // true
        if (signVerified) { //验签成功
            // 交易状态
            String trade_status = paramMap.get("trade_status");
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                // 正常的支付成功，我们应该更新交易记录状态
                paymentInfoService.updatePaymentStatus(paramMap, 2);//2是支付类型,支付宝支付
                return "success"; //接收通知完成业务后，一定返回"success"字符串给支付宝服务器，支付宝收到成功字符串。不再发消息了。交易结束。
            }

        } else {
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }

}
