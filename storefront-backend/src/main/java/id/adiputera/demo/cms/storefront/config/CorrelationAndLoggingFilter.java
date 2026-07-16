package id.adiputera.demo.cms.storefront.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter for injecting correlation ID into MDC and recording mandatory operation entry/exit logs.
 *
 * @author Yusuf F. Adiputera
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorrelationAndLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_OPERATION = "operation";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_DURATION = "duration";

    /**
     * Intercepts HTTP requests to inject MDC context variables and log operation lifecycles.
     *
     * @param request The servlet request.
     * @param response The servlet response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet error occurs.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader(CORRELATION_ID_HEADER))
                .filter(id -> !id.isBlank())
                .orElseGet(() -> UUID.randomUUID().toString());
        String userId = Optional.ofNullable(request.getHeader(USER_ID_HEADER))
                .filter(id -> !id.isBlank())
                .orElse("anonymous");
        String operation = request.getMethod() + " " + request.getRequestURI();

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_OPERATION, operation);
        MDC.put(MDC_USER_ID, userId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        long startTime = System.currentTimeMillis();
        log.info("Operation start: {}", operation);

        try {
            filterChain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(MDC_DURATION, String.valueOf(duration));
            log.info("Operation complete: {} (status: {}, duration: {} ms)", operation, response.getStatus(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(MDC_DURATION, String.valueOf(duration));
            log.error("Operation failure: {} (duration: {} ms) - Error: {}", operation, duration, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
