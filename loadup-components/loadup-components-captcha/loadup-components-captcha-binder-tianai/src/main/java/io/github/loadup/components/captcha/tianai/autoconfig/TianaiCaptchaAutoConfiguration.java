package io.github.loadup.components.captcha.tianai.autoconfig;

/*-
 * #%L
 * LoadUp Captcha Binder Tianai
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

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.ImageCaptchaProperties;
import cloud.tianai.captcha.application.TACBuilder;
import cloud.tianai.captcha.cache.CacheStore;
import cloud.tianai.captcha.cache.impl.LocalCacheStore;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.generator.ImageTransform;
import cloud.tianai.captcha.generator.impl.transform.Base64ImageTransform;
import cloud.tianai.captcha.interceptor.CaptchaInterceptor;
import cloud.tianai.captcha.interceptor.CaptchaInterceptorGroup;
import cloud.tianai.captcha.interceptor.impl.ParamCheckCaptchaInterceptor;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.ResourceProviders;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import io.github.loadup.components.captcha.CaptchaProvider;
import io.github.loadup.components.captcha.autoconfig.CaptchaAutoConfiguration;
import io.github.loadup.components.captcha.tianai.TianaiCaptchaProperties;
import io.github.loadup.components.captcha.tianai.TianaiCaptchaProvider;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for the tianai behavior captcha binder.
 *
 * <p>Assembles the tianai engine from its core classes (no official starter, which targets older
 * Spring Boot generations) and exposes the LoadUp {@link CaptchaProvider}.
 */
@AutoConfiguration(before = CaptchaAutoConfiguration.class)
@ConditionalOnClass(TACBuilder.class)
@ConditionalOnProperty(prefix = "loadup.captcha", name = "binder-type", havingValue = "tianai", matchIfMissing = true)
@EnableConfigurationProperties(TianaiCaptchaProperties.class)
public class TianaiCaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ResourceStore tianaiResourceStore() {
        return new LocalMemoryResourceStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ImageCaptchaResourceManager tianaiResourceManager(ResourceStore resourceStore) {
        return new DefaultImageCaptchaResourceManager(resourceStore, new ResourceProviders());
    }

    @Bean
    @ConditionalOnMissingBean
    public ImageTransform tianaiImageTransform() {
        return new Base64ImageTransform();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheStore tianaiCacheStore() {
        return new LocalCacheStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaInterceptor tianaiCaptchaInterceptor() {
        CaptchaInterceptorGroup group = new CaptchaInterceptorGroup();
        group.addInterceptor(new ParamCheckCaptchaInterceptor());
        return group;
    }

    @Bean
    @ConditionalOnMissingBean
    public ImageCaptchaApplication tianaiImageCaptchaApplication(
            ResourceStore resourceStore,
            ImageCaptchaResourceManager resourceManager,
            ImageTransform imageTransform,
            CacheStore cacheStore,
            CaptchaInterceptor interceptor,
            TianaiCaptchaProperties properties) {
        ImageCaptchaProperties engineProperties = new ImageCaptchaProperties();
        engineProperties.setExpire(toExpireMap(properties));
        TACBuilder builder = TACBuilder.builder()
                .setResourceStore(resourceStore)
                .setProp(engineProperties)
                .setCacheStore(cacheStore)
                .setTransform(imageTransform)
                .setInterceptor(interceptor);
        if (properties.isInitDefaultResource()) {
            builder.addDefaultTemplate();
            registerDefaultBackgroundResources(builder);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaProvider tianaiCaptchaProvider(
            ImageCaptchaApplication application, TianaiCaptchaProperties properties) {
        return new TianaiCaptchaProvider(application, properties.getDefaultType());
    }

    private static Map<String, Long> toExpireMap(TianaiCaptchaProperties properties) {
        Map<String, Long> expire = new LinkedHashMap<>();
        if (properties.getExpireSeconds().isEmpty()) {
            expire.put("default", 120L);
        } else {
            expire.putAll(properties.getExpireSeconds());
        }
        return expire;
    }

    /**
     * Register the background image bundled in the tianai jar for the captcha types that need
     * one. tianai's {@code DefaultBuiltInResources} only ships templates and fonts; without an
     * explicit background resource the generators fail at runtime.
     */
    private static void registerDefaultBackgroundResources(TACBuilder builder) {
        Resource background = new Resource("classpath", "META-INF/cut-image/resource/1.jpg");
        builder.addResource(CaptchaTypeConstant.SLIDER, background);
        builder.addResource(CaptchaTypeConstant.ROTATE, background);
        builder.addResource(CaptchaTypeConstant.CONCAT, background);
    }
}
