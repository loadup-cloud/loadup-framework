package io.github.loadup.modules.transfer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.transfer")
public class TransferTaskProperties {
    private long maxInputBytes = 20L * 1024 * 1024;
    private long maxOutputBytes = 100L * 1024 * 1024;

    public long getMaxInputBytes() { return maxInputBytes; }
    public void setMaxInputBytes(long maxInputBytes) {
        if (maxInputBytes < 1) throw new IllegalArgumentException("maxInputBytes must be positive");
        this.maxInputBytes = maxInputBytes;
    }
    public long getMaxOutputBytes() { return maxOutputBytes; }
    public void setMaxOutputBytes(long maxOutputBytes) {
        if (maxOutputBytes < 1) throw new IllegalArgumentException("maxOutputBytes must be positive");
        this.maxOutputBytes = maxOutputBytes;
    }
}
