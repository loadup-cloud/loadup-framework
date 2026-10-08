package io.github.loadup.components.gotone.channel.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.gotone.binder.sms.yunpian")
public class YunpianSmsConfig {
    private String apiKey;
    private String apiUrl = "https://sms.yunpian.com/v2/sms/single_send.json";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
