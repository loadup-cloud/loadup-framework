package io.github.loadup.components.tracer.example;

/*-
 * #%L
 * loadup-components-tracer
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

import io.github.loadup.components.tracer.TraceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "loadup.tracer.example", name = "enabled", havingValue = "true")
public class ExampleController {

  private final ExampleService exampleService;

  @GetMapping("/simple")
  public String simpleEndpoint(@RequestParam(defaultValue = "test") String input) {
    log.info("Received request with traceId: {}", TraceUtil.getTracerId());
    return exampleService.simpleOperation(input);
  }

  @PostMapping("/with-params")
  public String withParams(@RequestParam String param1, @RequestParam int param2) {
    return exampleService.operationWithParams(param1, param2);
  }

  @GetMapping("/nested")
  public String nestedOperations(@RequestParam String input) {
    return exampleService.nestedOperation(input);
  }

  @PostMapping("/manual")
  public String manualTracing(@RequestBody String data) {
    return exampleService.manualSpanOperation(data);
  }

  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
