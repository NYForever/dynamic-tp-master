package com.dtp.starter.extension.limiter.redis.autoconfigure;

import com.dtp.extension.limiter.redis.ratelimiter.NotifyRedisRateLimiterFilter;
import com.dtp.extension.limiter.redis.ratelimiter.RedisRateLimiter;
import com.dtp.extension.limiter.redis.ratelimiter.SlidingWindowRateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * RedisLimiterAutoConfiguration related
 *
 * @author yanhom
 * @since 1.0.8
 **/
@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisRateLimiter<List<Long>> redisScriptRateLimiter() {
        return new SlidingWindowRateLimiter();
    }

    @Bean
    @ConditionalOnMissingBean
    public NotifyRedisRateLimiterFilter notifyRedisRateLimiterFilter() {
        return new NotifyRedisRateLimiterFilter();
    }
}
