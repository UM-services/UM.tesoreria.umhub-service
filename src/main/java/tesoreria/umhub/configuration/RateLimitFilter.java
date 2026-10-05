package tesoreria.umhub.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite de tasa de requests por minuto (fixed window) para los endpoints de consulta
 * por numero de documento, que exponen PII: sin el, una API Key filtrada permite
 * barrer el rango de documentos (enumeracion). Ventana de 1 minuto por cliente
 * (X-Forwarded-For o IP remota). Configurable por app.rate-limit.*; aplica solo
 * debajo de /api/tesoreria/umhub/persona.
 */
@Component
@Order(2)
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    private final Cache<String, AtomicInteger> contadores = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(2))
            .maximumSize(100_000)
            .build();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !request.getRequestURI().startsWith("/api/tesoreria/umhub/persona");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long ventana = System.currentTimeMillis() / 60_000;
        String clave = ventana + ":" + cliente(request);
        int cantidad = contadores.get(clave, key -> new AtomicInteger()).incrementAndGet();

        if (cantidad > requestsPerMinute) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(Math.max(1, 60_000 - (System.currentTimeMillis() % 60_000)) / 1000));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"mensaje\": \"Limite de solicitudes excedido\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String cliente(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
