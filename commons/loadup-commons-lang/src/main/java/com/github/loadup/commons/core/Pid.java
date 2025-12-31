package com.github.loadup.commons.core;

/*-
 * #%L
 * loadup-commons-lang
 * %%
 * Copyright (C) 2022 - 2024 loadup_cloud
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

/**
 * 进程ID单例封装<br>
 *
 * @author Lise
 * @since 1.0.0
 */
import java.lang.management.ManagementFactory;

public enum Pid {
  INSTANCE;

  private final int pid;

  Pid() {
    this.pid = getPid();
  }

  /**
   * 获取当前进程ID，首先获取进程名称，读取@前的ID值，如果不存在，则读取进程名的hash值
   *
   * @throws RuntimeException 进程名称为空
   */
  private static int getPid() throws RuntimeException {
    final String processName = ManagementFactory.getRuntimeMXBean().getName();
    if (null == processName || 0 == processName.length()) {
      throw new RuntimeException("Process name is blank!");
    }
    final int atIndex = processName.indexOf('@');
    if (atIndex > 0) {
      return Integer.parseInt(processName.substring(0, atIndex));
    } else {
      return processName.hashCode();
    }
  }

  /** 获取PID值 */
  public int get() {
    return this.pid;
  }
}
