package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.port.NotificationChannelProviderPort;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NotificationProviderFactory {

    private final Map<NotificationProvider, NotificationChannelProviderPort> providers;

    public NotificationProviderFactory(List<NotificationChannelProviderPort> providerPorts) {
        this.providers = new EnumMap<>(NotificationProvider.class);

        providerPorts.forEach(providerPort ->
                this.providers.put(providerPort.provider(), providerPort)
        );
    }

    public NotificationChannelProviderPort getProvider(NotificationProvider provider) {
        NotificationChannelProviderPort port = providers.get(provider);

        if (port == null) {
            log.warn("event=notification.provider_not_supported provider={}", provider);

            throw new BaseException(
                    NotificationErrorCode.NOTIFICATION_PROVIDER_NOT_SUPPORTED
            );
        }

        return port;
    }
}