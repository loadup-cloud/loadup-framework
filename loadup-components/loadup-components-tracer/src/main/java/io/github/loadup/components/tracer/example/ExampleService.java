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
import io.github.loadup.components.tracer.annotation.Traced;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExampleService {

  @Traced(name = "ExampleService.simpleOperation")
  public String simpleOperation(String input) {
    log.info("Executing simple operation with input: {}", input);
    return "Processed: " + input;
  }

  @Traced(
      name = "ExampleService.operationWithParams",
      includeParameters = true,
      includeResult = true)
  public String operationWithParams(String param1, int param2) {
    log.info("Executing operation with params: {}, {}", param1, param2);
    return param1 + "-" + param2;
  }

  @Traced(
      name = "ExampleService.operationWithAttributes",
      attributes = {"type=business", "priority=high"})
  public void operationWithAttributes() {
    log.info("Executing operation with custom attributes");
  }

  public String manualSpanOperation(String data) {
    Span span = TraceUtil.createSpan("ExampleService.manualOperation");
    try {
      span.setAttribute("input.length", data.length());
      span.setAttribute("operation.type", "manual");
      log.info("TraceId: {}", TraceUtil.getTracerId());
      String result = processData(data);
      span.setAttribute("result.length", result.length());
      return result;
    } catch (Exception e) {
      span.recordException(e);
      throw e;
    } finally {
      span.end();
    }
  }

  @Traced(name = "ExampleService.nestedOperation")
  public String nestedOperation(String input) {
    log.info("Parent operation executing");
    String result = childOperation(input);
    return "Parent processed: " + result;
  }

  @Traced(name = "ExampleService.childOperation")
  private String childOperation(String input) {
    log.info("Child operation executing");
    return "Child processed: " + input;
  }

  private String processData(String data) {
    return data.toUpperCase();
  }
}
