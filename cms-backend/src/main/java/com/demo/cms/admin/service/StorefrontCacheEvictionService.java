package com.demo.cms.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service to clear both CMS and Storefront Redis caches
 * when content is updated through the CMS admin.
 *
 * Since both services share the same Redis instance,
 * we can directly evict storefront cache keys from here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorefrontCacheEvictionService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Evict all storefront-related cache entries.
     * Called after any CMS content mutation (create/update/delete).
     */
    public void evictStorefrontCaches() {
        evictByPattern("pages::*");
        evictByPattern("slots::*");
        evictByPattern("products::*");
        log.info("Storefront caches evicted");
    }

    private void evictByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Evicted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache keys for pattern '{}': {}", pattern, e.getMessage());
        }
    }
}
