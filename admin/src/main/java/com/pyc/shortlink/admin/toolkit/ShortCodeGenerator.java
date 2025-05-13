package com.pyc.shortlink.admin.toolkit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ShortCodeGenerator {

    private RedisTemplate redisTemplate;
    private RedisAtomicLong redisAtomicLong;

    private final String redisKey = "short_code_key";
    private final AtomicLong localId = new AtomicLong(0);
    private final int step = 1000; // 步长，根据短码长度调整
    private final int expireDays = 10; // 过期天数，根据短码长度调整
    private final int codeLength = 5; // 短码长度
    private long currentSegmentStart;
    private long currentSegmentEnd;

    @Autowired
    public ShortCodeGenerator(RedisConnectionFactory redisConnectionFactory , RedisTemplate redisTemplate) {
        this.redisAtomicLong = new RedisAtomicLong(redisKey, redisConnectionFactory);
        this.redisTemplate = redisTemplate;
    }

    public synchronized String getNextShortCode() {
        if (currentSegmentStart == 0 || localId.get() >= currentSegmentEnd) {
            long nextSegment = redisAtomicLong.addAndGet(step);
            currentSegmentStart = nextSegment - step;
            currentSegmentEnd = nextSegment;
            // 设置过期时间
            redisTemplate.expire(redisKey, expireDays * 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);
        }
        long id = localId.incrementAndGet();
//        long confusedId = confuse(id);
        return convertToBase62(id);
    }

//    private long confuse(long number) {
//        return number ^ 123456789; // 使用异或操作进行混淆
//    }

    private String convertToBase62(long number) {
        char[] chars = "p5msyAiV1lLO26IMgxDcewTJ9CH83XYBb0SNqW4hzQdnFEjUaPtvfKrGouZRk7".toCharArray();
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(chars[(int) (number % 62)]);
            number /= 62;
        }
        // 补足短码长度
        while (sb.length() < codeLength) {
            sb.append('0');
        }
        return sb.reverse().toString();
    }
}
