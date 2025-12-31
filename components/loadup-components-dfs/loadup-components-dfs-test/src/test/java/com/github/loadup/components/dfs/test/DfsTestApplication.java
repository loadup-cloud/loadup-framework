package com.github.loadup.components.dfs.test;

/*-
 * #%L
 * loadup-components-dfs-test
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Test application for DFS component testing */
@SpringBootApplication(scanBasePackages = "com.github.loadup.components")
@EntityScan(basePackages = "com.github.loadup.components.dfs.binder.database.entity")
@EnableJpaRepositories(basePackages = "com.github.loadup.components.dfs.binder.database.repository")
public class DfsTestApplication {
  public static void main(String[] args) {
    SpringApplication.run(DfsTestApplication.class, args);
  }
}
