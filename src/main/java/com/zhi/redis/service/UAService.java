package com.zhi.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.java2d.pipe.SpanShapeRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@Service
public class UAService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private static final String BASE_HEADER = "InputKeyAndValue";
    private static final String SIGN_IN_HEADER = "SIGN_IN_";

    private static final String DAU_HEADER = "DAU_";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");



    public String uaDemo(String s) {
        String hello = redisTemplate.opsForValue().get(s);
        if (hello == null) {
            redisTemplate.opsForValue().set(BASE_HEADER + s , s);
            hello = redisTemplate.opsForValue().get(BASE_HEADER + s);
        }

        return hello;
    }

    public boolean signIn(String username, int day){
        Boolean aBoolean = redisTemplate.opsForValue().setBit(SIGN_IN_HEADER + username, day, true);

        if (aBoolean != null && !aBoolean)
            globalSignUsedHyperLogLog(username);

        return true;
    }

    public Long checkSignInRecordByUsername(String username){
         return redisTemplate.execute((RedisCallback<Long>) x -> x.bitCount(username.getBytes()));
    }

    @Deprecated
    private void globalSignUsedBitmap(String username) {
        redisTemplate.opsForValue().setBit(DAU_HEADER + simpleDateFormat.format(new Date()), username.hashCode(), true);
    }

    private boolean globalSignUsedHyperLogLog(String username) {
        String key = DAU_HEADER + simpleDateFormat.format(new Date());
        redisTemplate.opsForHyperLogLog().add(key, username);
        return true;
    }

    public Long getDAU() {
        String key = DAU_HEADER + simpleDateFormat.format(new Date());
        return redisTemplate.opsForHyperLogLog().size(key);
    }
}
