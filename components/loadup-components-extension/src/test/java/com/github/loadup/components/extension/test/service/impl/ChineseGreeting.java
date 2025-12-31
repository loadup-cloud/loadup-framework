package com.github.loadup.components.extension.test.service.impl;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.test.service.GreetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Extension(bizCode = "ChineseGreeting")
public class ChineseGreeting implements GreetingService {
  @Override
  public String greet() {
    log.info("ChineseGreeting:{}", "你好");
    return "你好";
  }
}
