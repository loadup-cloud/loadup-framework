package io.github.loadup.components.gotone.test.service;

/*-
 * #%L
 * loadup-components-gotone-test
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

import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.test.GotoneTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * NotificationService 集成测试
 *
 * <p>使用 Testify 框架进行数据驱动测试
 * <p>测试用例配置在 YAML 文件中
 */
public class NotificationServiceTest extends GotoneTestBase {

    @Autowired
    private NotificationService notificationService;

    @Test(dataProvider = "testifyData")
    public void testSendNotification_Success(
        NotificationRequest request) {

        runTest(() -> notificationService.send(request));
    }

    @Test(dataProvider = "testifyData")
    public void testSendNotification_WithIdempotence(
        NotificationRequest request) {

        runTest(() -> notificationService.send(request));
    }

    @Test(dataProvider = "testifyData")
    public void testSendNotification_ServiceNotFound(String serviceCode) {

        runTest(() -> {
            NotificationRequest request = NotificationRequest.builder()
                .serviceCode(serviceCode)
                .receivers(List.of("test@example.com"))
                .templateParams(Map.of())
                .build();

            return notificationService.send(request);
        });
    }

    @Test(dataProvider = "testifyData")
    public void testSendNotification_WithSpecifiedChannels(
        NotificationRequest request) {

        runTest(() -> notificationService.send(request));
    }

    @Test(dataProvider = "testifyData")
    public void testSendNotification_MultiChannel(NotificationRequest request) {

        runTest(() -> notificationService.send(request));
    }
}

