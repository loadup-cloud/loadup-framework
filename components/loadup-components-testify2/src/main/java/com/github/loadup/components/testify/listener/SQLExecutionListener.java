package com.github.loadup.components.testify.listener;

import com.github.loadup.components.testify.annotation.DbCheck;
import com.github.loadup.components.testify.annotation.DbProcess;
import com.github.loadup.components.testify.executor.SqlFileExecutor;
import com.github.loadup.components.testify.verifier.DbDataVerifier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.testng.*;

import javax.sql.DataSource;
import java.lang.reflect.Method;

public class SQLExecutionListener implements IInvokedMethodListener {

    @SneakyThrows
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();

            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess anno = testMethod.getAnnotation(DbProcess.class);
                if (StringUtils.isNotBlank(anno.init())) {
                    DataSource ds = getDataSource(testResult);
                    SqlFileExecutor.execute(anno.init(), ds);
                }
            }

            if (testMethod.isAnnotationPresent(DbCheck.class)) {
                DbCheck anno = testMethod.getAnnotation(DbCheck.class);
                if (anno.before()) {
                    DataSource ds = getDataSource(testResult);
                    DbDataVerifier.verify(anno.value(), ds);
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            Method testMethod = method.getTestMethod().getConstructorOrMethod().getMethod();

            if (testMethod.isAnnotationPresent(DbCheck.class)) {
                DbCheck anno = testMethod.getAnnotation(DbCheck.class);
                if (anno.after()) {
                    DataSource ds = getDataSource(testResult);
                    DbDataVerifier.verify(anno.value(), ds);
                }
            }

            if (testMethod.isAnnotationPresent(DbProcess.class)) {
                DbProcess anno = testMethod.getAnnotation(DbProcess.class);
                if (StringUtils.isNotBlank(anno.clear())) {
                    DataSource ds = getDataSource(testResult);
                    SqlFileExecutor.execute(anno.clear(), ds);
                }
            }


        }
    }

    private DataSource getDataSource(ITestResult result) {
        ApplicationContext ctx = (ApplicationContext) result.getAttribute("applicationContext");
        if (ctx == null) throw new IllegalStateException("No Spring context in test.");
        return ctx.getBean(DataSource.class);
    }
}
