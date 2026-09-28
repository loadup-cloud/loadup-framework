package io.github.loadup.modules.upms.domain.valueobject;

/**
 * User Status Value Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserStatus {

    public static final short NORMAL = 1;
    public static final short DISABLED = 0;
    public static final short LOCKED = 2;

    private short status;

    private String description;

    public static UserStatus normal() {
        return new UserStatus(NORMAL, "正常");
    }

    public static UserStatus disabled() {
        return new UserStatus(DISABLED, "停用");
    }

    public static UserStatus locked() {
        return new UserStatus(LOCKED, "锁定");
    }

    public static UserStatus of(short status) {
        return switch (status) {
            case NORMAL -> normal();
            case DISABLED -> disabled();
            case LOCKED -> locked();
            default -> throw new IllegalArgumentException("Invalid user status: " + status);
        };
    }

    public boolean isNormal() {
        return status == NORMAL;
    }

    public boolean isDisabled() {
        return status == DISABLED;
    }

    public boolean isLocked() {
        return status == LOCKED;
    }

    public UserStatus(short status, String description) {
        this.status = status;
        this.description = description;
    }

    public UserStatus() {}

    public void setStatus(short status) {
        this.status = status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return "UserStatus{status=" + status + ", description='" + description + "'}";
    }
}
