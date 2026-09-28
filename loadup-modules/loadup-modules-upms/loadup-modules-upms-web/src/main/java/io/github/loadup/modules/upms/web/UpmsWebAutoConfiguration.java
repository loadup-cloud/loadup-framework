/*-
 * #%L
 * LoadUp UPMS Web Adapter
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
package io.github.loadup.modules.upms.web;

import io.github.loadup.modules.upms.app.autoconfigure.UpmsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(
        after = UpmsAutoConfiguration.class,
        beforeName = "org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration")
@Import({
    AuthenticationController.class,
    UserController.class,
    RoleController.class,
    PermissionController.class,
    DepartmentController.class,
    UpmsResponseAdvice.class
})
public class UpmsWebAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ErrorController.class)
    public UpmsApiErrorController upmsApiErrorController() {
        return new UpmsApiErrorController();
    }

    @Bean
    public WebMvcConfigurer upmsApiPathPrefix() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(PathMatchConfigurer configurer) {
                configurer.addPathPrefix(
                        "/api",
                        type -> type.getPackageName().startsWith("io.github.loadup.modules.upms.web")
                                && !UpmsApiErrorController.class.isAssignableFrom(type));
            }
        };
    }
}
