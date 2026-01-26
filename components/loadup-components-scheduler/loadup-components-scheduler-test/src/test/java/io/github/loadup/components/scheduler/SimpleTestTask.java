package io.github.loadup.components.scheduler;

import io.github.loadup.components.scheduler.annotation.DistributedScheduler;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SimpleTestTask {
  public static AtomicInteger a = new AtomicInteger(0);

  @DistributedScheduler(name = "SimpleTestTask", cron = "* * * * * ?")
  public void executeClean() {
    System.out.println("run simple test every 1s...");
    a.set(a.intValue() + 1);
  }
}
