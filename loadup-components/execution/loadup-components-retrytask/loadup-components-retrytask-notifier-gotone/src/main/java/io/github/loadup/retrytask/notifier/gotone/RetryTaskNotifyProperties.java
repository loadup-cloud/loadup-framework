package io.github.loadup.retrytask.notifier.gotone;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the gotone-backed retry task notifier ({@code loadup.retrytask.notify.*}).
 *
 * @param enabled whether the notifier is active
 * @param serviceCode the gotone service code routing the failure alert to its channels
 * @param receivers the alert receivers (email addresses, phone numbers, webhook targets)
 */
@ConfigurationProperties(prefix = "loadup.retrytask.notify")
public class RetryTaskNotifyProperties {

    private boolean enabled = true;
    private String serviceCode;
    private List<String> receivers = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }
}
