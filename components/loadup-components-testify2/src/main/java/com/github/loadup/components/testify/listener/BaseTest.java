package com.github.loadup.components.testify.listener;

import com.github.loadup.components.testify.TestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;

@SpringBootTest(classes = TestApplication.class)
public class BaseTest extends AbstractTestNGSpringContextTests {

    @Autowired
    protected ApplicationContext applicationContext;

    @BeforeMethod
    public void beforeMethod(ITestContext context, ITestResult result) {
        // 将 Spring ApplicationContext 放入 TestNG 上下文中
        result.setAttribute("applicationContext", applicationContext);
    }
}
