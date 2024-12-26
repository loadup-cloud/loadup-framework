//package com.github.loadup.components.retrytask.test;
//
/// *-
// * #%L
// * loadup-components-retrytask
// * %%
// * Copyright (C) 2022 - 2023 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import com.github.loadup.capability.common.test.BaseTest;
//import com.github.loadup.components.retrytask.RetryComponentService;
//import com.github.loadup.components.retrytask.model.RetryTask;
//import com.github.loadup.components.retrytask.model.RetryTaskRequest;
//import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import javax.annotation.Resource;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.time.DateUtils;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//@Slf4j
////@ActiveProfiles("remote")
//public class RetryTaskTest extends BaseTest {
//    @Resource
//    RetryComponentService retryComponentService;
//    @Resource
//    RetryTaskRepository   retryTaskRepository;
//
//    @Test
//    void testExponentialTask() throws InterruptedException {
//        // 0,40,80,160,320
//        String bizType = "EXPONENTIAL";
//        List<SleepPeriod> list = new ArrayList<>();
//        //30s
//        list.add(new SleepPeriod(30, 40));
//        //70s
//        list.add(new SleepPeriod(40, 120));
//
//        runTask(bizType, list);
//
//    }
//
//    @Test
//    void testFibonacciWaitStrategyTask() {
//        // 0,20,20,40,60
//        String bizType = "FIBONACCI";
//        List<SleepPeriod> list = new ArrayList<>();
//        //15s
//        list.add(new SleepPeriod(15, 20));
//        //35s
//        list.add(new SleepPeriod(20, 40));
//        //55s
//        list.add(new SleepPeriod(20, 80));
//        runTask(bizType, list);
//    }
//
//    @Data
//    @AllArgsConstructor
//    static class SleepPeriod {
//        private int sleep;
//        private int total;
//    }
//
//    void runTask(String bizType, List<SleepPeriod> sleepPeriods) {
//        String bizId = UUID.randomUUID().toString();
//        try {
//            RetryTaskRequest req = new RetryTaskRequest();
//            req.setBizType(bizType);
//            req.setBizId(bizId);
//            req.setBizContext("xxx");
//            retryComponentService.delete(bizId, bizType);
//            RetryTask register = retryComponentService.register(req);
//            for (int i = 0; i < sleepPeriods.size(); i++) {
//                SleepPeriod map = sleepPeriods.get(i);
//                Thread.sleep(map.getSleep() * 1000L);
//                register = retryTaskRepository.findByBizId(bizId, bizType);
//                Assertions.assertEquals(i + 1, register.getExecutedTimes());
//                assertDateEquals(DateUtils.addSeconds(register.getCreatedTime(), map.getTotal()), register.getNextExecuteTime());
//            }
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            retryTaskRepository.delete(bizId, bizType);
//        }
//
//    }
//
//}
