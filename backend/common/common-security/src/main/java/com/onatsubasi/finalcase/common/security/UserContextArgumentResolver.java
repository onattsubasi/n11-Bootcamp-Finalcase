package com.onatsubasi.finalcase.common.security;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.exception.CommonErrorCode;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasCurrentUserAnnotation = parameter.hasParameterAnnotation(CurrentUser.class);
        boolean isUserContextType = UserContext.class.isAssignableFrom(parameter.getParameterType());

        return hasCurrentUserAnnotation && isUserContextType;
    }

    @Override
    public UserContext resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        boolean required = isRequired(parameter);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserContext userContext) {
            return requireIfNeeded(userContext, required);
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            return requireIfNeeded(UserContext.anonymous(), required);
        }

        UserContext userContext = new UserContext(
                parseUserId(request.getHeader(PlatformHeaders.X_USER_ID)),
                normalize(request.getHeader(PlatformHeaders.X_USER_EMAIL)),
                parseRoles(request.getHeader(PlatformHeaders.X_USER_ROLES))
        );

        return requireIfNeeded(userContext, required);
    }

    private boolean isRequired(MethodParameter parameter) {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        return annotation == null || annotation.required();
    }

    private UserContext requireIfNeeded(UserContext userContext, boolean required) {
        if (required && (userContext == null || !userContext.isAuthenticated())) {
            throw new BaseException(CommonErrorCode.UNAUTHORIZED);
        }

        return userContext == null ? UserContext.anonymous() : userContext;
    }

    private UUID parseUserId(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(rawUserId.trim());
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
                .collect(Collectors.toUnmodifiableSet());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}