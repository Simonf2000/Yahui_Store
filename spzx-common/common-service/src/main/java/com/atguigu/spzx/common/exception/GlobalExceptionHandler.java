package com.atguigu.spzx.common.exception;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/20/19:34
 * @Description:
 */

import com.atguigu.spzx.model.vo.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 com.atguigu.spzx.common.exception
 * 统一异常处理类
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.build(null , 201,"出现了异常") ;
    }

    @ExceptionHandler(value = GuiguException.class)     // 处理自定义异常
    @ResponseBody
    public Result error(GuiguException exception) {
        exception.printStackTrace();
        return Result.build(null , exception.getResultCodeEnum()) ;
    }
}