package com.schoolerp.usermanagement.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ModelCacheConfig {
    // For now rely on default Spring Cache; RedisCacheManager configured via RedisConfig
}
