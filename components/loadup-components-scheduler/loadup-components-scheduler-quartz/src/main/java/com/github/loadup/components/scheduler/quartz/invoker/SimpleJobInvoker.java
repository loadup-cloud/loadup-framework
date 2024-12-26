package com.github.loadup.components.scheduler.quartz.invoker;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.core.JobContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SimpleJobInvoker implements JobInvoker {
    @Override
    public void invoke(JobContext jobContext) {
        TaskWrapper taskWrapper = jobContext.getTaskWrapper();
        Method method = taskWrapper.getMethod();
        Object bean = taskWrapper.getObject();
        try {
            method.invoke(bean);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
