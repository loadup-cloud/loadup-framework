package com.github.loadup.components.tracer.example;

import com.github.loadup.components.tracer.TraceUtil;
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

