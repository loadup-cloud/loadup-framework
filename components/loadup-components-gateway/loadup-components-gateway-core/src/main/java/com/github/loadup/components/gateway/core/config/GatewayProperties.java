package com.github.loadup.components.gateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Gateway Configuration Properties
 */
@Data
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {

    /**
     * 是否启用网关
     */
    private boolean enabled = true;

    /**
     * 网关路由配置
     */
    private List<RouteDefinition> routes = new ArrayList<>();

    /**
     * 限流配置
     */
    private RateLimitProperties rateLimit = new RateLimitProperties();

    /**
     * 认证配置
     */
    private SecurityProperties security = new SecurityProperties();

    /**
     * 路由定义
     */
    @Data
    public static class RouteDefinition {
        /**
         * 路由ID
         */
        private String id;

        /**
         * 路由URI
         */
        private String uri;

        /**
         * 路由断言
         */
        private List<PredicateDefinition> predicates = new ArrayList<>();

        /**
         * 路由过滤器
         */
        private List<FilterDefinition> filters = new ArrayList<>();

        /**
         * 路由元数据
         */
        private List<MetadataDefinition> metadata = new ArrayList<>();
    }

    @Data
    public static class PredicateDefinition {
        private String       name;
        private List<String> args = new ArrayList<>();
    }

    @Data
    public static class FilterDefinition {
        private String       name;
        private List<String> args = new ArrayList<>();
    }

    @Data
    public static class MetadataDefinition {
        private String name;
        private String value;
    }

    @Data
    public static class RateLimitProperties {
        private boolean enabled      = true;
        private String  type         = "sentinel";  // sentinel or redis
        private int     defaultLimit = 100;    // default requests per second
        private int     timeWindow   = 1;        // time window in seconds
    }

    @Data
    public static class SecurityProperties {
        private boolean      enabled         = true;
        private String       jwtSecret;
        private long         tokenExpiration = 3600; // token expiration in seconds
        private List<String> ignoredPaths    = new ArrayList<>();
    }
}
