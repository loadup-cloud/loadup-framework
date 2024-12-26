package com.github.loadup.components.scheduler.quartz.task;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleTask {
    public static String testData = "";

    @DistributedScheduler(name = "xxx", cron = "* * * * * ?")
    public void test() {
        SimpleTask.testData = "123";
    }
}
