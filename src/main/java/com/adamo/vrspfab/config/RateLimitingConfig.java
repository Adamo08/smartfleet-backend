package com.adamo.vrspfab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Rate limiting configuration to prevent abuse of API endpoints.
 */
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {

    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor() {
        return new RateLimitingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor())
                .addPathPatterns("/reservations/**", "/auth/**", "/payments/**");
    }

    public static class RateLimitingInterceptor implements HandlerInterceptor {
        
        private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
        private static final int MAX_REQUESTS_PER_MINUTE = 60;
        private static final int MAX_REQUESTS_PER_HOUR = 1000;
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String clientId = getClientId(request);
            RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientId, k -> new RateLimitInfo());
            
            LocalDateTime now = LocalDateTime.now();
            
            // Clean old entries
            if (ChronoUnit.MINUTES.between(rateLimitInfo.lastMinuteReset, now) >= 1) {
                rateLimitInfo.minuteCount.set(0);
                rateLimitInfo.lastMinuteReset = now;
            }
            
            if (ChronoUnit.HOURS.between(rateLimitInfo.lastHourReset, now) >= 1) {
                rateLimitInfo.hourCount.set(0);
                rateLimitInfo.lastHourReset = now;
            }
            
            // Check rate limits
            if (rateLimitInfo.minuteCount.get() >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "60");
                return false;
            }
            
            if (rateLimitInfo.hourCount.get() >= MAX_REQUESTS_PER_HOUR) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "3600");
                return false;
            }
            
            // Increment counters
            rateLimitInfo.minuteCount.incrementAndGet();
            rateLimitInfo.hourCount.incrementAndGet();
            
            return true;
        }
        
        private String getClientId(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        
        private static class RateLimitInfo {
            AtomicInteger minuteCount = new AtomicInteger(0);
            AtomicInteger hourCount = new AtomicInteger(0);
            LocalDateTime lastMinuteReset = LocalDateTime.now();
            LocalDateTime lastHourReset = LocalDateTime.now();
        }
    }
}
