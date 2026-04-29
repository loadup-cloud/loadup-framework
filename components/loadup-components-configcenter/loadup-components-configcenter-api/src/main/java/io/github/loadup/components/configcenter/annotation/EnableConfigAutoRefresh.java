package io.github.loadup.components.configcenter.annotation;

import io.github.loadup.components.configcenter.autoconfig.ConfigAutoRefreshConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * 开启配置中心自动刷新能力。
 *
 * <p>在 Spring Boot 启动类或配置类上标注此注解，当配置中心检测到变更（{@link io.github.loadup.components.configcenter.model.ConfigChangeEvent}）时，
 * 自动调用 {@code ContextRefresher.refresh()} 刷新 {@code @Value} / {@code @ConfigurationProperties}。
 *
 * <p>依赖 {@code spring-cloud-context}（需自行引入），否则本注解无效。
 *
 * <pre>
 * {@literal @}SpringBootApplication
 * {@literal @}EnableConfigAutoRefresh
 * public class MyApplication {}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigAutoRefreshConfiguration.class)
public @interface EnableConfigAutoRefresh {}
