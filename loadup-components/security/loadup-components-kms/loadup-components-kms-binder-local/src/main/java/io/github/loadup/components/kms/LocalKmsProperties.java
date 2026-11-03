package io.github.loadup.components.kms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Optional file-backed KeyStore loading; passwords are read by environment-property name. */
@ConfigurationProperties(prefix = "loadup.kms.local")
public class LocalKmsProperties {
    private String keyStoreLocation;
    private String keyStoreType = "PKCS12";
    private String keyStorePasswordEnv = "KMS_KEYSTORE_PASSWORD";
    private String keyPasswordEnv;

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStorePasswordEnv() {
        return keyStorePasswordEnv;
    }

    public void setKeyStorePasswordEnv(String keyStorePasswordEnv) {
        this.keyStorePasswordEnv = keyStorePasswordEnv;
    }

    public String getKeyPasswordEnv() {
        return keyPasswordEnv;
    }

    public void setKeyPasswordEnv(String keyPasswordEnv) {
        this.keyPasswordEnv = keyPasswordEnv;
    }
}
