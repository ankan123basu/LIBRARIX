package com.librarix.ratelimit;

import com.librarix.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Rate Limit Interceptor — wires SlidingWindowRateLimiter into the Spring MVC pipeline.
 *
 * Intercepts all /api/** requests. Identifies users by JWT principal or IP fallback.
 * Returns 429 Too Many Requests with Retry-After header when limit is exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final SlidingWindowRateLimiter rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userKey = resolveUserKey(request);

        if (!rateLimiter.allowRequest(userKey)) {
            long retryAfter = rateLimiter.getRetryAfterMs(userKey);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter / 1000));
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimiter.getMaxRequests()));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Rate limit exceeded\",\"retryAfterMs\":" + retryAfter +
                    ",\"limit\":" + rateLimiter.getMaxRequests() +
                    ",\"windowMs\":" + rateLimiter.getWindowMs() + "}"
            );
            return false;
        }

        // Add rate limit headers to successful responses
        response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimiter.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimiter.getRemainingRequests(userKey)));

        return true;
    }

    private String resolveUserKey(HttpServletRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return "user:" + principal.getId();
            }
        } catch (Exception ignored) {
            // Fall through to IP-based rate limiting
        }
        return "ip:" + request.getRemoteAddr();
    }
}
