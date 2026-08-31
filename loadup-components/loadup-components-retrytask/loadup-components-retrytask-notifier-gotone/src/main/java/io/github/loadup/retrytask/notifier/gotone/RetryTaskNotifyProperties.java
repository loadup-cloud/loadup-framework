/*-
 * #%L
 * Loadup Components Retrytask Notifier Gotone
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
