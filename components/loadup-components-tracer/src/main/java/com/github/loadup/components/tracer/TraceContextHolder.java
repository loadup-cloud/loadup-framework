package com.github.loadup.components.tracer;

public class TraceContextHolder {
    private static final TraceContext TRACE_CONTEXT = new TraceContext();

    public TraceContextHolder() {
    }

    public static TraceContext getSofaTraceContext() {
        return TRACE_CONTEXT;
    }
}
