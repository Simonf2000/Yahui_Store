package com.atguigu.spzx.common.anno;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/15:11
 * @Description:
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记哪些方法可以增加分布式锁。
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface GuiGuCache {

    /**
     * 缓存key的前缀
     * @return
     */
    public String cacheKey() ;  // 用来让使用者指定缓存的key的名称

    public String lockName() default "" ;         // 分布式锁的名称
    public boolean enableLock() default false ;   // 是否需要开启分布式锁
}