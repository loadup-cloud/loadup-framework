package io.github.loadup.commons.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.log")
public class LoadupLogProperties {

    private boolean enabled = true;

    private boolean includeTraceContext = true;

    private String consolePattern = LogContext.DEFAULT_CONSOLE_PATTERN;

    private String filePattern = LogContext.DEFAULT_CONSOLE_PATTERN;

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIncludeTraceContext() {
        return includeTraceContext;
    }

    public void setIncludeTraceContext(boolean includeTraceContext) {
        this.includeTraceContext = includeTraceContext;
    }

    public String getConsolePattern() {
        return consolePattern;
    }

    public void setConsolePattern(String consolePattern) {
        this.consolePattern = consolePattern;
    }
}
