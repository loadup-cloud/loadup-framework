package io.github.loadup.framework.service;

import org.springframework.stereotype.Service;

@Service
public class DemoService {

    public Response getData() {
        return new Response("hello world");
    }

    static class Response {
        private final String message;

        Response(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
