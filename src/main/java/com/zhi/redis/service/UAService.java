package com.zhi.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UAService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static String BASE_HEADER = "InputKeyAndValue";

    public String uaDemo(String s) {
        String hello = redisTemplate.opsForValue().get(s);
        if (hello == null) {
            redisTemplate.opsForValue().set(BASE_HEADER + s , s);
            hello = redisTemplate.opsForValue().get(BASE_HEADER + s);
        }

        return hello;
    }

    public boolean signIn(String username, int day){
        redisTemplate.opsForValue().setBit(username, day, true);
        return true;
    }

    public Long checkSignInRecordByUsername(String username){
         return redisTemplate.execute((RedisCallback<Long>) x -> x.bitCount(username.getBytes()));
    }
}
