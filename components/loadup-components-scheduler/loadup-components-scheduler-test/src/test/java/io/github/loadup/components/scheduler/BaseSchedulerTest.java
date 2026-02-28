package io.github.loadup.components.scheduler;

/*-
 * #%L
 * Loadup Components Scheduler Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import static org.apache.commons.lang3.ThreadUtils.sleep;

import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.framework.api.annotation.BindingClient;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseSchedulerTest {
    @BindingClient("simplejob-biz-type")
    protected SchedulerBinding simpleJobBinding;

    protected void safeSleep(long second) {
        try {
            sleep(Duration.ofSeconds(second));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
