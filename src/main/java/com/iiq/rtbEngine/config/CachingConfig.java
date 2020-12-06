package com.iiq.rtbEngine.config;


import com.google.common.cache.CacheBuilder;
import com.iiq.rtbEngine.util.CommonConfig;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(name, CacheBuilder.newBuilder().expireAfterWrite(CommonConfig.cacheManagerGlobalExpirationSeconds, TimeUnit.SECONDS)
//                        .maximumSize(CommonConfig.cacheManagerGlobalMaxElementsInMemory)
                        .build().asMap(), false);
            }
        };

        return cacheManager;
    }
}




