/*-
 * #%L
 * Testify Demo
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.testify.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.testify.starter.scenario.TestScenario;
import io.github.loadup.testify.test.TestApplication;
import io.github.loadup.testify.test.model.Order;
import io.github.loadup.testify.test.model.User;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private TestScenario scenario;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void cleanUp() {
        scenario.clean("users");
    }

    @Test
    void createUser_shouldPersistUserAndUseMockedOrderService() {
        String userId = scenario.uuid();
        String userName = "Ada";
        User user = new User(userId, userName, "ada@example.com");
        Order order = new Order();
        order.setOrderId(userId);
        order.setOrderName(userName);
        when(orderService.createOrder(userId, userName)).thenReturn(order);

        User saved = userService.createUser(user);

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getOrder()).isSameAs(order);
        verify(orderService).createOrder(userId, userName);
        scenario.assertDb("users").where("user_id", userId).exists().has("status", "ACTIVE");
    }

    @Test
    void getUserById_shouldReturnUserInsertedByScenario() {
        String userId = scenario.uuid();
        scenario.insert(
                "users",
                Map.of(
                        "user_id", userId,
                        "user_name", "Grace",
                        "email", "grace@example.com",
                        "status", "ACTIVE",
                        "created_at", LocalDateTime.now()));

        User found = userService.getUserById(userId);

        assertThat(found.getUserName()).isEqualTo("Grace");
    }
}
