package io.awesome.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CachingConfig {

    public static final String ALL_USERS_CACHE = "users";
    public static final String ALL_APPLICANT_MASTER_RECORDS = "applicantMasterRecords";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(ALL_USERS_CACHE, ALL_APPLICANT_MASTER_RECORDS);
    }
}