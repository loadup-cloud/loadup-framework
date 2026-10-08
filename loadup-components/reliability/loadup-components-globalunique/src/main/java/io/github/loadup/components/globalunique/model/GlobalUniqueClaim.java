package io.github.loadup.components.globalunique.model;

/** Data required to claim one tenant-scoped business key. */
public record GlobalUniqueClaim(String bizType, String uniqueKey, String bizId, String requestData) {
    private static final int BIZ_TYPE_MAX_LENGTH = 64;
    private static final int UNIQUE_KEY_MAX_LENGTH = 255;
    private static final int BIZ_ID_MAX_LENGTH = 100;

    public GlobalUniqueClaim {
        bizType = requireText(bizType, "bizType", BIZ_TYPE_MAX_LENGTH);
        uniqueKey = requireText(uniqueKey, "uniqueKey", UNIQUE_KEY_MAX_LENGTH);
        if (bizId != null && bizId.length() > BIZ_ID_MAX_LENGTH) {
            throw new IllegalArgumentException("bizId must not exceed " + BIZ_ID_MAX_LENGTH + " characters");
        }
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters");
        }
        return normalized;
    }
}
