package com.smartcommunity.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class LocalCacheService {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expireAtMs > now) {
            return (T) cached.value;
        }
        T value = loader.get();
        long expireAtMs = now + Math.max(ttl.toMillis(), 1L);
        cache.put(key, new CacheEntry(value, expireAtMs));
        return value;
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void evictByPrefix(String prefix) {
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    private record CacheEntry(Object value, long expireAtMs) {
    }
}
