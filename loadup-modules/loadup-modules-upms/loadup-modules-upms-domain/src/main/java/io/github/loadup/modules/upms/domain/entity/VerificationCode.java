package io.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

/**
 * 用户OAuth第三方账号绑定实体
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class VerificationCode {

    private LocalDateTime expiryTime;
    private String code;
    private String target;
    private String type;
    private int attempts;
    private LocalDateTime createdAt;

    public ChronoLocalDateTime<?> getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
