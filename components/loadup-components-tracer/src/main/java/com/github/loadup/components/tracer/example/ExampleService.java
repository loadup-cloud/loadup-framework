package com.github.loadup.components.tracer.example;

import com.github.loadup.components.tracer.TraceUtil;
import com.github.loadup.components.tracer.annotation.Traced;
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

    @Traced(name = "ExampleService.operationWithParams", includeParameters = true, includeResult = true)
    public String operationWithParams(String param1, int param2) {
        log.info("Executing operation with params: {}, {}", param1, param2);
        return param1 + "-" + param2;
    }

    @Traced(name = "ExampleService.operationWithAttributes", attributes = {"type=business", "priority=high"})
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
