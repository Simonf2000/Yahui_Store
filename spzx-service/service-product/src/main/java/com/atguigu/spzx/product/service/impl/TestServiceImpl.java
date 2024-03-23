package com.atguigu.spzx.product.service.impl;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/03/23/10:30
 * @Description:
 */
import com.atguigu.spzx.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    RedisTemplate<String,String> redisTemplate;


    @Autowired
    RedissonClient redissonClient;

    public void testLock() {
        try {
            RLock lock = redissonClient.getLock("lock"); //获取锁对象
            boolean tryLock = lock.tryLock(3, 20, TimeUnit.SECONDS);//加锁  默认30秒自动释放
            if(tryLock){
                try {
                    //1.从Redis缓存中获取key="num"的值
                    String value = redisTemplate.opsForValue().get("num");
                    if (StringUtils.isEmpty(value)) {
                        redisTemplate.opsForValue().set("num" , "1");
                    }

                    //2.对获取到值进行+1操作
                    int num = Integer.parseInt(value);
                    redisTemplate.opsForValue().set("num", String.valueOf(++num));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock(); //释放锁
                }
            }else{
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                testLock();
            }



        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     **常见问题6**

     问题说明：业务执行的时间大于锁的过期时间，导致其他的线程在锁自动释放完毕以后加锁成功，分布式锁失效。

     解决方案：提供续期机制(看门狗机制)

     常见手段：创建一个定时任务，每隔200ms给锁续期到初始过期时间
     */
    private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(8); //线程池

   /* @Override
    public void testLock() {
        try {
            //Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent("lock", "1"); //抢锁，true表示抢锁成功
            //问题2：在释放锁那一刹那服务器宕机，导致无法释放锁，增加锁自动释放时间。
            //问题3：获取锁之后，服务器突然宕机，导致没有设置过期时间成功。导致死锁。解决办法，就是，获取锁和设置时间必须是原子的。使用带4个参数的setIfAbsent方法即可。
            //redisTemplate.expire("lock" , 30 , TimeUnit.SECONDS) ;          // 给锁设置过期时间，让锁具备自动释放的功能

            //问题4说明：在释放锁的时候把别的线程上的锁给释放了，怎么解决? 锁的标识唯一，是自己标识可以删除锁。不是自己的不能删除锁。





            String uuid = UUID.randomUUID().toString().replace("-", "");
            Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent("lock", uuid,10,TimeUnit.SECONDS); //抢锁，true表示抢锁成功

            //从线程池中获取一个线程来执行任务，启动任务后200毫秒，开始每个200毫秒执行一次任务。
            scheduledThreadPool.scheduleAtFixedRate(()->{
                redisTemplate.expire("lock",10,TimeUnit.SECONDS);
            },2000,2000,TimeUnit.MILLISECONDS);

            if(setIfAbsent){ //保存多个操作原子性
                try {
                    //1.从Redis缓存中获取key="num"的值
                    String value = redisTemplate.opsForValue().get("num");
                    if (StringUtils.isEmpty(value)) {
                        redisTemplate.opsForValue().set("num" , "1");
                    }

                    //2.对获取到值进行+1操作
                    int num = Integer.parseInt(value);
                    redisTemplate.opsForValue().set("num", String.valueOf(++num));
                    //int i = 1/0;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } finally {

                    //问题5说明：判断锁的操作和释放锁的操作，被其他线程加锁线程所中断，导致释放的是别的线程所对应的锁，怎么解决? 解决方案：使用lua脚本保证判断和释放锁操作的原子性
//                    String lockValue = redisTemplate.opsForValue().get("lock");
//                    if(uuid.equals(lockValue)){ //相当说明是自己的锁。不相当说明锁被是否。得到锁的值是别人的。
//                        redisTemplate.delete("lock"); //问题1：解决异常锁无法释放，变成死锁的问题。
//                    }

                    // 解锁
                    String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                            "then\n" +
                            "    return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "    return 0\n" +
                            "end" ;

                    // 执行lua脚本
                    Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);

                    // 判断执行结果
                    if (result == 1) {
                        log.info("锁是自己的，删除锁成功...");
                    }else {
                        log.error("锁是别人的，删除锁失败...");
                    }
                }

            }else{
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                testLock(); //递归，自旋
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }*/

    /*    @Override
    public void testLock() {
        try {
            //本地锁只能在同一个JVM中多线程有效。分布式系统多个JVM无效的。
            synchronized (this) {
                //1.从Redis缓存中获取key="num"的值
                String value = redisTemplate.opsForValue().get("num");
                if (StringUtils.isEmpty(value)) {
                    redisTemplate.opsForValue().set("num" , "1");
                }

                //2.对获取到值进行+1操作
                int num = Integer.parseInt(value);
                redisTemplate.opsForValue().set("num", String.valueOf(++num));
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }*/

}

