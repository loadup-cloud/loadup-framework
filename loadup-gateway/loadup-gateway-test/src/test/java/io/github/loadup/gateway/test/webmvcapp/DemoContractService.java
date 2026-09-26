package io.github.loadup.gateway.test.webmvcapp;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service("demoContractService")
public class DemoContractService implements DemoContract {
    @Override
    @PreAuthorize("permitAll()")
    public String reply(@PathVariable("id") String id) {
        return "contract:" + id;
    }
}
