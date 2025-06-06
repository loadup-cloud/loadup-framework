package com.github.loadup.components.testify.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DbProcess {
    String init(); // SQL语句
    String clear(); // SQL语句
}
