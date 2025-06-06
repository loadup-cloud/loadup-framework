package com.github.loadup.components.testify.listener;

import com.github.loadup.components.testify.annotation.DbCheck;
import com.github.loadup.components.testify.annotation.DbProcess;
import com.github.loadup.components.testify.executor.SqlFileExecutor;
import com.github.loadup.components.testify.verifier.DbDataVerifier;
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

            // 处理 @DbProcess(init = "...")
            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess annotation = testMethod.getAnnotation(DbProcess.class);
                String initSqlFile = annotation.init();
                if (StringUtils.isNotBlank(initSqlFile)) {
                    DataSource dataSource = getDataSourceFromSpringContext(testResult);
                    SqlFileExecutor.execute(initSqlFile, dataSource);
                }
            }

            // 处理 @DbCheck(before = true)
            if (testMethod.isAnnotationPresent(DbCheck.class)) {
                DbCheck annotation = testMethod.getAnnotation(DbCheck.class);
                if (annotation.before()) {
                    String checkSqlFile = annotation.value();
                    if (StringUtils.isNotBlank(checkSqlFile)) {
                        DataSource dataSource = getDataSourceFromSpringContext(testResult);
                        DbDataVerifier.verify(checkSqlFile, dataSource);
                    }
                }
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();

            // 处理 @DbCheck(after = true)
            if (testMethod.isAnnotationPresent(DbCheck.class)) {
                DbCheck annotation = testMethod.getAnnotation(DbCheck.class);
                if (annotation.after()) {
                    String checkSqlFile = annotation.value();
                    if (StringUtils.isNotBlank(checkSqlFile)) {
                        DataSource dataSource = getDataSourceFromSpringContext(testResult);
                        DbDataVerifier.verify(checkSqlFile, dataSource);
                    }
                }
            }

            // 处理 @DbProcess(clear = "...")
            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess annotation = testMethod.getAnnotation(DbProcess.class);
                String clearSqlFile = annotation.clear();
                if (StringUtils.isNotBlank(clearSqlFile)) {
                    DataSource dataSource = getDataSourceFromSpringContext(testResult);
                    SqlFileExecutor.execute(clearSqlFile, dataSource);
                }
            }

        }
    }

    /**
     * 从 TestNG 上下文中获取 Spring 的 ApplicationContext，并从中获取 DataSource
     */
    private DataSource getDataSourceFromSpringContext(ITestResult testResult) {
        ApplicationContext context = (ApplicationContext) testResult.getAttribute("applicationContext");
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not found in test context.");
        }
        return context.getBean(DataSource.class);
    }
}
