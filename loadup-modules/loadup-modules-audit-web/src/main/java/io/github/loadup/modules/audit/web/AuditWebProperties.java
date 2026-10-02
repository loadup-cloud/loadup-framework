package io.github.loadup.modules.audit.web;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** HTTP capture settings. No paths are captured until the application opts in. */
@ConfigurationProperties(prefix = "loadup.audit.web")
public class AuditWebProperties {
    private boolean enabled = true;
    private List<String> includePaths = new ArrayList<>();
    private List<String> includeMethods = new ArrayList<>(List.of("POST", "PUT", "PATCH", "DELETE"));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths == null ? new ArrayList<>() : new ArrayList<>(includePaths);
    }

    public List<String> getIncludeMethods() {
        return includeMethods;
    }

    public void setIncludeMethods(List<String> includeMethods) {
        this.includeMethods = includeMethods == null ? new ArrayList<>() : new ArrayList<>(includeMethods);
    }
}
