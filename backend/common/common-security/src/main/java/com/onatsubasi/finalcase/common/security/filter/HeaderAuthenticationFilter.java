package com.onatsubasi.finalcase.common.security.filter;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

   @Override
   protected void doFilterInternal(
         HttpServletRequest request,
         HttpServletResponse response,
         FilterChain filterChain) throws ServletException, IOException {
      try {
         putCorrelationIdToMdc(request);

         String rawUserId = normalize(request.getHeader(PlatformHeaders.X_USER_ID));

         if (rawUserId == null) {
            filterChain.doFilter(request, response);
            return;
         }

         UUID userId = parseUserId(rawUserId);

         if (userId == null) {
            log.warn("Invalid X-User-Id header received");
            filterChain.doFilter(request, response);
            return;
         }

         String email = normalize(request.getHeader(PlatformHeaders.X_USER_EMAIL));
         Set<String> roles = parseRoles(request.getHeader(PlatformHeaders.X_USER_ROLES));

         UserContext userContext = new UserContext(userId, email, roles);

         UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
               userContext,
               null,
               roles.stream()
                     .map(this::toAuthority)
                     .collect(Collectors.toUnmodifiableSet()));

         authentication.setDetails(
               new WebAuthenticationDetailsSource().buildDetails(request));

         SecurityContextHolder.getContext().setAuthentication(authentication);
         MDC.put("userId", userId.toString());

         filterChain.doFilter(request, response);
      } finally {
         SecurityContextHolder.clearContext();
         MDC.remove("userId");
         MDC.remove("correlationId");
      }
   }

   private void putCorrelationIdToMdc(HttpServletRequest request) {
      String correlationId = normalize(request.getHeader(PlatformHeaders.X_CORRELATION_ID));

      if (correlationId != null) {
         MDC.put("correlationId", correlationId);
      }
   }

   private UUID parseUserId(String rawUserId) {
      try {
         return UUID.fromString(rawUserId);
      } catch (IllegalArgumentException ex) {
         return null;
      }
   }

   private Set<String> parseRoles(String rawRoles) {
      if (rawRoles == null || rawRoles.isBlank()) {
         return Collections.emptySet();
      }

      return Arrays.stream(rawRoles.split(","))
            .map(String::trim)
            .filter(role -> !role.isBlank())
            .map(this::normalizeRole)
            .collect(Collectors.toUnmodifiableSet());
   }

   private String normalizeRole(String role) {
      String normalized = role.trim().toUpperCase();

      if (normalized.startsWith("ROLE_")) {
         return normalized.substring("ROLE_".length());
      }

      return normalized;
   }

   private SimpleGrantedAuthority toAuthority(String role) {
      return new SimpleGrantedAuthority("ROLE_" + normalizeRole(role));
   }

   private String normalize(String value) {
      return value == null || value.isBlank() ? null : value.trim();
   }
}