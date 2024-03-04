package com.atguigu.spzx.common.log.aspect;

import com.atguigu.spzx.common.log.annotation.Log;
import com.atguigu.spzx.common.log.com.atguigu.spzx.common.log.util.LogUtil;
import com.atguigu.spzx.common.log.service.AsyncOperLogService;
import com.atguigu.spzx.model.entity.system.SysOperLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/02/8:41
 * @Description:
 */
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Autowired
    AsyncOperLogService asyncOperLogService;

    @Around(value = "@annotation(syslog)") //切入点表达式，匹配方法上使用了@Log注解的目标方法。
    public Object doAroundAdvice(ProceedingJoinPoint proceedingJoinPoint, Log syslog){

        SysOperLog sysOperLog = new SysOperLog();

        String title = syslog.title();
        System.out.println("title = " + title);
        log.info("LogAspect - doAroundAdvice - title = " + title);
        Object result = null;

        LogUtil.beforeHandleLog(syslog,proceedingJoinPoint,sysOperLog);

        try {
            result = proceedingJoinPoint.proceed(); //执行目标方法

            LogUtil.afterHandlLog(syslog,result,sysOperLog,0,null);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.afterHandlLog(syslog,result,sysOperLog,1,e.getMessage());
            //不能生吞异常
            throw new RuntimeException("日志切面捕获目标程序异常e="+e.getMessage());
        } finally {
            asyncOperLogService.save(sysOperLog);
        }
        return result;
    }
}
