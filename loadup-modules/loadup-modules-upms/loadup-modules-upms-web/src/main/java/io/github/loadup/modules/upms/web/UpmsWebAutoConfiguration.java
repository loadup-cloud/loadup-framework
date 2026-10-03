package io.github.loadup.modules.upms.web;

import io.github.loadup.modules.upms.app.autoconfigure.UpmsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(after = UpmsAutoConfiguration.class)
@Import({
    AuthenticationController.class,
    AccountSecurityController.class,
    UserController.class,
    RoleController.class,
    PermissionController.class,
    DepartmentController.class
})
public class UpmsWebAutoConfiguration {
    @Bean
    public WebMvcConfigurer upmsApiPathPrefix() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                configurer.addPathPrefix(
                        "/api", HandlerTypePredicate.forBasePackage("io.github.loadup.modules.upms.web"));
            }
        };
    }
}
