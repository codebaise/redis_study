package com.zhi.distlock.service;

import com.zhi.distlock.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DistService {

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    private static final String LOCK_HEADER = "LOCK_HEADER_";
    private static final String GOOD_HEADER = "GOOD_HEADER_";

    public void addInventory(String goodName, int num) {
        redisTemplate.opsForValue().set(GOOD_HEADER + goodName, num);
    }

    public String sellGoods(String goodName) throws Exception {
        String lockKey = LOCK_HEADER + "SELL_GOODS";
        String uuid = UUID.randomUUID().toString();
        try {
            // 如下代码不是原子性的
//        String s = (String) redisTemplate.opsForValue().get(lockKey);
//        if (s != null) {
//            redisTemplate.opsForValue().set(lockKey, uuid);
//        }
            // 设置超时时间, 防止服务端宕机, 锁没有释放
            // 但是这里就又需要考虑到续期的情况
            Boolean setAccept = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 30L, TimeUnit.SECONDS);
            if (Objects.equals(setAccept, true)) {
                // true 说明加锁成功
                String goodKey = GOOD_HEADER + goodName;
                Integer inventory = (Integer) redisTemplate.opsForValue().get(goodKey);
                inventory = inventory == null ? 0 : inventory;
                if (inventory > 0) {
                    redisTemplate.opsForValue().decrement(goodKey, 1);
                    return "购买成功, 还有" + (inventory - 1) + "个商品!";
                } else {
                    return "购买失败, 当前商品暂无库存!!!";
                }
            } else {
                // 加锁失败
                return "当前商品太火爆, 请稍后再试!!!";
            }
        } finally {
            // 这里需要使用原子性检查 是否是自己加的锁, 防止张冠李戴
            // 使用lua脚本来达到原子性
//            if(Objects.equals(redisTemplate.opsForValue().get(lockKey), uuid))
//                redisTemplate.delete(lockKey);

            try (Jedis jedis = RedisUtils.getJedis()) {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                        "then " +
                        "return redis.call('del', KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
                Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(uuid));
                if ("1".equals(result.toString())) {
                    System.out.println("------del REDIS_LOCK_KEY success");
                } else {
                    System.out.println("------del REDIS_LOCK_KEY error");
                }
            }
        }
    }

}
