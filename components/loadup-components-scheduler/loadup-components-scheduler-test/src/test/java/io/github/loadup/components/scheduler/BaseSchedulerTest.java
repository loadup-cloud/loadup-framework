package io.github.loadup.components.scheduler;

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
