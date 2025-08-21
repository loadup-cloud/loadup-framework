package com.github.loadup.components.gateway.core.filter.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.github.loadup.components.gateway.core.config.GatewayProperties;
import com.github.loadup.components.gateway.core.filter.GatewayFilter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Rate Limit Filter
 * 基于Sentinel的限流过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GatewayFilter {

    private final GatewayProperties gatewayProperties;

    @PostConstruct
    public void init() {
        if (!isEnabled()) {
            return;
        }

        List<FlowRule> rules = new ArrayList<>();
        // 创建默认的限流规则
        FlowRule rule = new FlowRule();
        rule.setResource("default");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(gatewayProperties.getRateLimit().getDefaultLimit());
        rules.add(rule);

        // 为每个路由创建限流规则
        gatewayProperties.getRoutes().forEach(route -> {
            FlowRule routeRule = new FlowRule();
            routeRule.setResource(route.getId());
            routeRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            // 从路由元数据中获取限流配置，如果没有则使用默认值
            route.getMetadata().stream()
                .filter(meta -> "rateLimit".equals(meta.getName()))
                .findFirst()
                .ifPresent(meta -> routeRule.setCount(Double.parseDouble(meta.getValue())));
            rules.add(routeRule);
        });

        FlowRuleManager.loadRules(rules);
    }

    @Override
    public int getOrder() {
        return -90; // 限流过滤器在认证之后执行
    }

    @Override
    public boolean isEnabled() {
        return gatewayProperties.getRateLimit().isEnabled();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!isEnabled()) {
            return true;
        }

        String routeId = (String) request.getAttribute("route_id");
        String resource = routeId != null ? routeId : "default";

        try (Entry entry = SphU.entry(resource)) {
            return true;
        } catch (BlockException e) {
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return false;
        }
    }
}
