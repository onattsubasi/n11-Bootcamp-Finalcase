package com.onatsubasi.finalcase.user.support;

import com.onatsubasi.finalcase.common.security.UserContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class FixedUserContextArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserContext userContext;

    public FixedUserContextArgumentResolver(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserContext.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        return userContext;
    }
}
