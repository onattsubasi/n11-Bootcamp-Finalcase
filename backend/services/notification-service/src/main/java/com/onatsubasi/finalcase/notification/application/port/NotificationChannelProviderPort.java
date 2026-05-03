package com.onatsubasi.finalcase.notification.application.port;

import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendCommand;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendResult;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;

public interface NotificationChannelProviderPort {

    NotificationProvider provider();

    NotificationChannel channel();

    NotificationProviderSendResult send(NotificationProviderSendCommand command);
}