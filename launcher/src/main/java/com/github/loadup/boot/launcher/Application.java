/*
 * Ant Group
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.github.loadup.boot.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author lise
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.github.loadup", "com.alibaba.cola"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

	}
}
