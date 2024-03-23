package com.atguigu.spzx.product.controller;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/10:29
 * @Description:
 */
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product/test")
public class TestController {

    //  注入服务层方法
    @Autowired
    private TestService testService;

    /**
     * 测试分布式锁
     *
     * @return
     */
    @GetMapping("testLock")
    public Result testLock() {
        testService.testLock();
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }
}