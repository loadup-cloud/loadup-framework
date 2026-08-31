package io.github.loadup.gateway.test.webmvcapp;

import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Backend bean targeted by the {@code bean://} route used in integration tests.
 */
@Service("demoEchoService")
public class DemoEchoService {

    public String echo(Map<String, Object> body) {
        Object name = body == null ? null : body.get("name");
        return "echo:" + (name == null ? "null" : name);
    }
}
