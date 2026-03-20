package br.com.sistema.bot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableAsync
public class CacheConfig {

    // ====================================================
    // cacheManager - Configura Caffeine com TTL de 50 minutos para o token Hubsoft
    // (expires_in = 3600s, renovamos com margem de 10 minutos)
    // ====================================================
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("hubsoft_token");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(50, TimeUnit.MINUTES)
                .maximumSize(10));
        return manager;
    }
}
