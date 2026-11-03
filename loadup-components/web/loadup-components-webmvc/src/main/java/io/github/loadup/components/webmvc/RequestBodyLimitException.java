package io.github.loadup.components.webmvc;

/** Transport rejection before deserialization or business invocation. */
final class RequestBodyLimitException extends RuntimeException {
    RequestBodyLimitException() {
        super("Request body limit exceeded");
    }
}
