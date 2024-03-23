package com.atguigu.spzx.common.aspect;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/15:14
 * @Description:
 */
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.common.anno.GuiGuCache;
import com.atguigu.spzx.common.constant.RedisConst;
import com.atguigu.spzx.model.vo.h5.ProductItemVo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class GuiguCacheAspect {


    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Autowired
    RedissonClient redissonClient;



    // SpelExpressionParser是线程安全的
    private static final SpelExpressionParser spelExpressionParser = new SpelExpressionParser() ;

    // 获取缓存key
    public <T> T paraseExpression(ProceedingJoinPoint proceedingJoinPoint , String cacheKey , Class<T>  clazz) {
        Expression expression = spelExpressionParser.parseExpression(cacheKey, ParserContext.TEMPLATE_EXPRESSION);
        EvaluationContext evaluationContext = new StandardEvaluationContext() ;
        evaluationContext.setVariable("params" , proceedingJoinPoint.getArgs());
        T cacheKeyData = expression.getValue(evaluationContext, clazz);
        return cacheKeyData ;
    }


    // 获取目标方法的返回值类型
    public Type getMethodType(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature() ;
        Method method = methodSignature.getMethod();
        Type genericReturnType = method.getGenericReturnType();
        return genericReturnType ;
    }


    /**
     * 代码改造，变成通用代码
     * @param proceedingJoinPoint
     * @param guiguCache
     * @return
     * @throws InterruptedException
     */
    @Around(value = "@annotation(guiguCache)")
    public Object cacheAroundAspect(ProceedingJoinPoint proceedingJoinPoint, GuiGuCache guiguCache) throws InterruptedException {

        //*****BloomFilter过滤器判断 start**********暂时不动***************************************
//        Long skuId = Long.parseLong(proceedingJoinPoint.getArgs()[0].toString());
//        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
//        if(!bloomFilter.contains(skuId)){
//            log.info("布隆过滤器中没有这个数据:skuId="+skuId);
//            return new ProductItemVo();
//        }
        //******BloomFilter过滤器判断 end***********暂时不动*************************************

        String cacheKeyExpr = guiguCache.cacheKey(); //RedisConst.SKUKEY_PREFIX + "#{#params[0]}" + RedisConst.SKUKEY_SUFFIX
        String cacheKey = paraseExpression(proceedingJoinPoint, cacheKeyExpr, String.class);

        Type methodReturnType = getMethodType(proceedingJoinPoint);

        //1.先找redis缓存，有直接返回
        Object value = null;
        String jsonStr = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(jsonStr)) {
            value = JSON.parseObject(jsonStr, methodReturnType);
            log.info("from redis cacheKey:"+cacheKey);
            log.info("from redis value:"+value);
            return value;
        }


        boolean enableLock = guiguCache.enableLock();
        if(enableLock){ //需要开启分布式锁

            String lockNameExpr = guiguCache.lockName();
            String lockNameKey = paraseExpression(proceedingJoinPoint, lockNameExpr, String.class);

            RLock lock = redissonClient.getLock(lockNameKey);
            //lock.lock(); //一直获取锁，获取不到锁一直等待。如果获取到锁默认30秒有效。看门狗机制： 1/3时间续期
            //lock.lock(20,TimeUnit.SECONDS); //一直获取锁，获取不到锁一直等待。锁的有效时间20秒。到时间自动释放。没有看门狗机制。
            //boolean tryLock = lock.tryLock(); //尝试一次获取锁，获取不到继续执行无锁代码。如果获取到锁默认30秒有效。看门狗机制： 1/3时间续期
            boolean tryLock = lock.tryLock(3,20,TimeUnit.SECONDS); //抢锁等待3秒，抢不到继续执行无锁代码，锁的有效时间20秒。到时间自动释放。没有看门狗机制。
            if(tryLock){ //获取锁成功
                try {
                    //2.没有则查询数据库，然后，放在缓存，下次利用缓存提高效率。
                    Object result = proceedingJoinPoint.proceed();//放行
                    if(result==null){
                        result = new Object();
                        redisTemplate.opsForValue()
                                .set(cacheKey,JSON.toJSONString(result),
                                        RedisConst.SKUKEY_EMPTY_TIMEOUT, TimeUnit.SECONDS); //空值只缓存30秒
                        log.info("数据来自-mysql,但是为null,存储默认值到Redis:"+result);
                    }else{
                        redisTemplate.opsForValue()
                                .set(cacheKey,JSON.toJSONString(result),
                                        RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS); //非空值只缓存24*60*60秒
                        log.info("数据来自-mysql,不为null,存储实际数据到Redis:"+result);
                    }
                    //TimeUnit.SECONDS.sleep(1000);
                    return result;
                } catch (Throwable e) {
                    System.out.println("GuiguCacheAspect = " + e.getMessage());
                    //e.printStackTrace();
                    throw new RuntimeException("GuiguCacheAspect切面异常e.getMessage()="+e.getMessage());
                } finally {
                    try {
                        lock.unlock(); //释放锁
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.info("锁不存在，自动释放，执行lock.unlock()锁释放抛出异常");
                    }
                }
            }
        }else{ //无需使用分布式锁
            try {
                //2.没有则查询数据库，然后，放在缓存，下次利用缓存提高效率。
                Object result = proceedingJoinPoint.proceed();//放行
                if(result==null){
                    result = new Object();
                    redisTemplate.opsForValue()
                            .set(cacheKey,JSON.toJSONString(result),
                                    RedisConst.SKUKEY_EMPTY_TIMEOUT, TimeUnit.SECONDS); //空值只缓存30秒
                    log.info("数据来自-mysql,但是为null,存储默认值到Redis:"+result);
                }else{
                    redisTemplate.opsForValue()
                            .set(cacheKey,JSON.toJSONString(result),
                                    RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS); //非空值只缓存24*60*60秒
                    log.info("数据来自-mysql,不为null,存储实际数据到Redis:"+result);
                }
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return new Object();
    }



    /**
     *  切面通知代码：
     *      目前不具备通用性。需要继续改造。
     *          例如：
     *              1.缓存key不通用
     *              2.JSON.parseObject(productItemVoJsonStr, ProductItemVo.class);代码反序列类型不通用
     *
     */
    /*@Around(value = "@annotation(guiguCache)")
    public Object cacheAroundAspect(ProceedingJoinPoint proceedingJoinPoint, GuiGuCache guiguCache) throws InterruptedException {

        Long skuId = Long.parseLong(proceedingJoinPoint.getArgs()[0].toString());

        //*****BloomFilter过滤器判断 start*************************************************
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.PRODUCT_BLOOM_FILTER);
        if(!bloomFilter.contains(skuId)){
            log.info("布隆过滤器中没有这个数据:skuId="+skuId);
            return new ProductItemVo();
        }

        //******BloomFilter过滤器判断 end************************************************

        //1.先找redis缓存，有直接返回
        ProductItemVo vo = null;
        String productItemVoJsonStr = redisTemplate.opsForValue().get(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX);
        if (StringUtils.hasText(productItemVoJsonStr)) {
            vo = JSON.parseObject(productItemVoJsonStr, ProductItemVo.class);
            log.info("商品详情数据来自-redis:"+vo);
            return vo;
        }

        String key = RedisConst.PRODUCT_LOCK_SUFFIX+skuId;
        RLock lock = redissonClient.getLock(key);
        boolean tryLock = lock.tryLock(3,20,TimeUnit.SECONDS); //一次性获取锁，获取不到代码继续执行。
        //boolean tryLock = lock.tryLock();
        if(tryLock){
            try {
                //2.没有则查询数据库，然后，放在缓存，下次利用缓存提高效率。
                Object result = proceedingJoinPoint.proceed();//放行
                if(result==null){
                    result = new ProductItemVo();
                    redisTemplate.opsForValue()
                            .set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX,JSON.toJSONString(result),
                                    RedisConst.SKUKEY_EMPTY_TIMEOUT, TimeUnit.SECONDS); //空值只缓存30秒
                    log.info("商品详情数据来自-mysql,但是为null,存储默认值到Redis:"+result);
                }else{
                    redisTemplate.opsForValue()
                            .set(RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX,JSON.toJSONString(result),
                                    RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS); //非空值只缓存24*60*60秒
                    log.info("商品详情数据来自-mysql:"+result);
                }
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                try {
                    lock.unlock(); //释放锁
                } catch (Exception e) {
                    log.info("锁不存在，自动释放，执行lock.unlock()锁释放抛出异常");
                }
            }
        }*//*else{
            cacheAroundAspect(proceedingJoinPoint,guiguCache);
        }*//*

        return new Object();
    }*/
}