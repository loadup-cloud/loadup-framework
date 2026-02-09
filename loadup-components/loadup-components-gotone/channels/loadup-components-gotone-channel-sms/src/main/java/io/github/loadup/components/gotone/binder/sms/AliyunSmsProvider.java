package io.github.loadup.components.gotone.binder.sms;

/*-
 * #%L
 * loadup-components-gotone-channel-sms
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.gotone.api.NotificationChannelProvider;
import io.github.loadup.components.gotone.enums.NotificationChannel;
import io.github.loadup.components.gotone.enums.NotificationStatus;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/** Aliyun SMS Channel Provider (示例实现，需要集成阿里云 SDK) */
@Slf4j
public class AliyunSmsProvider implements NotificationChannelProvider {

    @Value("${loadup.gotone.sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${loadup.gotone.sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${loadup.gotone.sms.aliyun.sign-name:}")
    private String signName;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
    @Override
    public ChannelSendResponse send(ChannelSendRequest request) {

        try {
            // TODO: 集成阿里云 SMS SDK
            log.info(">>> [GOTONE] Sending SMS via Aliyun to: {}, content: {}", request.getReceivers(), request.getContent());

            // 模拟发送
            return ChannelSendResponse.builder()
                   .build();

        } catch (Exception e) {
            log.error(">>> [GOTONE] Failed to send SMS via Aliyun", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return accessKeyId != null && !accessKeyId.isEmpty();
    }

    @Override
    public String getProviderName() {
        return "aliyun";
    }



    private ChannelSendResponse buildErrorResponse(String errorMessage) {
        return ChannelSendResponse.builder()
               .build();
    }
}
