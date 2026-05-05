package com.toiec.demo.aspect;




import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.toiec.demo.annotation.RateLimit;
import com.toiec.demo.security.UserPrincipal;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = getKey(rateLimit);
        Bucket bucket = bucketCache.get(key, k -> createNewBucket(rateLimit));
        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Quá nhiều yêu cầu, vui lòng thử lại sau.");
        }
    }

    private String getKey(RateLimit rateLimit) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        String userId = getCurrentUserId();
        return rateLimit.name() + ":" + ip + ":" + (userId != null ? userId : "anonymous");
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getId();
        }
        return null;
    }

    private Bucket createNewBucket(RateLimit rateLimit) {
        Refill refill = Refill.greedy(rateLimit.refillTokens(), Duration.ofMinutes(rateLimit.refillPeriodMinutes()));
        Bandwidth limit = Bandwidth.classic(rateLimit.capacity(), refill);
        return Bucket.builder().addLimit(limit).build();
    }
}