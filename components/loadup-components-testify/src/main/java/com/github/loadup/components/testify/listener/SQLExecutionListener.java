package com.github.loadup.components.testify.listener;

import com.github.loadup.components.testify.annotation.DbProcess;
import com.github.loadup.components.testify.executor.SqlFileExecutor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.testng.*;

import javax.sql.DataSource;
import java.lang.reflect.Method;

public class SQLExecutionListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess annotation = testMethod.getAnnotation(DbProcess.class);
                String initSqlFile = annotation.init();
if(StringUtils.isNotBlank(initSqlFile)){
                // 获取 Spring 容器中的 DataSource
                DataSource dataSource = getDataSourceFromSpringContext(testResult);

                // 执行初始化 SQL
                SqlFileExecutor.execute(initSqlFile, dataSource);
            }
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();
            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess annotation = testMethod.getAnnotation(DbProcess.class);
                String clearSqlFile = annotation.clear();
                if(StringUtils.isNotBlank(clearSqlFile)){

                // 获取 Spring 容器中的 DataSource
                DataSource dataSource = getDataSourceFromSpringContext(testResult);

                // 执行清理 SQL
                SqlFileExecutor.execute(clearSqlFile, dataSource);
            }
            }
        }
    }

    /**
     * 从 TestNG 上下文中获取 Spring 的 ApplicationContext，并从中获取 DataSource
     */
    private DataSource getDataSourceFromSpringContext(ITestResult testResult) {
        // 假设你在测试类中将 ApplicationContext 存入了 testResult 的 attribute 中
        ApplicationContext context = (ApplicationContext) testResult.getAttribute("applicationContext");
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not found in test context.");
        }

        return context.getBean(DataSource.class);
    }
}
