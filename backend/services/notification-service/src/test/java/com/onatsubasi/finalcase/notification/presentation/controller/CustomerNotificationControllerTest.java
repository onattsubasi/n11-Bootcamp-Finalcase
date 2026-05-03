package com.onatsubasi.finalcase.notification.presentation.controller;

import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.notification.application.dto.response.UnreadNotificationCountResponse;
import com.onatsubasi.finalcase.notification.application.service.NotificationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerNotificationControllerTest {

    private final NotificationQueryService queryService = mock(NotificationQueryService.class);
    private final UUID userId = UUID.randomUUID();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CustomerNotificationController(queryService))
            .setCustomArgumentResolvers(new TestCurrentUserResolver(userId))
            .build();

    @Test
    void unreadCountUsesCurrentUserFromHeaderContext() throws Exception {
        when(queryService.getUnreadCount(userId)).thenReturn(new UnreadNotificationCountResponse(3));

        mockMvc.perform(get("/api/customer/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unread count returned"))
                .andExpect(jsonPath("$.data.count").value(3));

        verify(queryService).getUnreadCount(userId);
    }

    private static class TestCurrentUserResolver implements HandlerMethodArgumentResolver {
        private final UUID userId;

        private TestCurrentUserResolver(UUID userId) {
            this.userId = userId;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class)
                    && parameter.getParameterType().equals(UserContext.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return new UserContext(userId, "customer@example.com", Set.of("CUSTOMER"));
        }
    }
}
