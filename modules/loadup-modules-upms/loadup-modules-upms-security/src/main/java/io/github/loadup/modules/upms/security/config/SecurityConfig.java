package io.github.loadup.modules.upms.security.config;

import io.github.loadup.modules.upms.security.filter.JwtAuthenticationFilter;
import io.github.loadup.modules.upms.security.handler.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(UpmsSecurityProperties.class)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final UserDetailsService userDetailsService;
  private final UpmsSecurityProperties upmsSecurityProperties;
  private final RestAuthenticationEntryPoint authenticationEntryPoint;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> {
              // Public endpoints from configuration
              String[] whitelist = upmsSecurityProperties.getWhitelist();
              if (whitelist != null && whitelist.length > 0) {
                auth.requestMatchers(whitelist).permitAll();
              }

              // Default public endpoints
              auth.requestMatchers(
                      "/api/v1/users/**",
                      "/api/v1/auth/**",
                      "/api/v1/auth/login",
                      "/api/v1/auth/register",
                      "/api/v1/auth/refresh-token",
                      "/api/v1/auth/forgot-password",
                      "/api/v1/auth/reset-password",
                      "/api/v1/captcha/**",
                      "/v3/api-docs/**",
                      "/swagger-ui/**",
                      "/doc.html",
                      "/swagger-ui.html")
                  .permitAll();

              // All other requests require authentication
              auth.anyRequest().authenticated();
            })
        // 4. 异常处理配置
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    // 1. 关联我们的自定义获取用户逻辑
    authProvider.setUserDetailsService(userDetailsService);
    // 2. 关联加密方式
    authProvider.setPasswordEncoder(passwordEncoder());
    // 3. 关键：不隐藏“用户不存在”异常，方便全局异常处理器捕获具体原因
    authProvider.setHideUserNotFoundExceptions(false);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
