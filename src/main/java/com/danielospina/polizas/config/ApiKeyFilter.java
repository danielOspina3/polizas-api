package com.danielospina.polizas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

/** Seguridad mínima: toda petición debe traer el header x-api-key con la llave configurada. */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "x-api-key";

    private final byte[] apiKeyEsperada;

    public ApiKeyFilter(@Value("${app.seguridad.api-key}") String apiKey) {
        this.apiKeyEsperada = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // La consola de H2 es solo una herramienta de desarrollo
        return request.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKeyRecibida = request.getHeader(HEADER_API_KEY);
        if (apiKeyRecibida == null || !esValida(apiKeyRecibida)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                    {"timestamp":"%s","status":401,"error":"Unauthorized","mensaje":"Header x-api-key ausente o inválido"}"""
                    .formatted(LocalDateTime.now()));
            return;
        }
        chain.doFilter(request, response);
    }

    /** Comparación en tiempo constante para no filtrar información por tiempos de respuesta. */
    private boolean esValida(String apiKeyRecibida) {
        return MessageDigest.isEqual(apiKeyEsperada, apiKeyRecibida.getBytes(StandardCharsets.UTF_8));
    }
}
