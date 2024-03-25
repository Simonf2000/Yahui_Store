package com.atguigu.spzx.user.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/25/10:46
 * @Description:
 */
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.common.util.HttpUtils;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.user.service.SmsService;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Override
    public void sendValidateCode(String phone) {

        String code = redisTemplate.opsForValue().get(RedisConst.PHONE_CODE_PREFIX + phone);
        if(StringUtils.hasText(code)) {
            return;
        }


        String validateCode = RandomStringUtils.randomNumeric(4);      // 生成验证码

        redisTemplate.opsForValue().set(RedisConst.PHONE_CODE_PREFIX + phone,validateCode,5, TimeUnit.MINUTES);

        sendSms(phone,validateCode);
    }

    // 发送短信方法
    private void sendSms(String phone, String validateCode) {

        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "1111111111111111111111111111111111";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:"+validateCode+",**minute**:5");

        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "1111111111111111");
        querys.put("templateId", "111111111111111");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
