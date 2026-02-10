package io.github.loadup.testify.starter.config;

/*-
 * #%L
 * Testify Spring Boot Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "loadup.testify")
public class TestifyProperties {
    /** Testcontainers 基础设施配置 */
    private TestcontainersProperties testcontainers = new TestcontainersProperties();

    /** 全局变量池 */
    private Map<String, Object> variables = new LinkedHashMap<>();

    /** 测试输入参数，Key 为方法参数名或索引 */
    private Map<String, Object> input = new LinkedHashMap<>();

    /** 前置脚本或数据准备 */
    private Map<String, Object> setup = new LinkedHashMap<>();

    /** Mock 规则定义 */
    private List<MockDefinition> mocks = new ArrayList<>();

    /** 预期结果定义 */
    private ExpectDefinition expect = new ExpectDefinition();

    @Data
    public static class TestcontainersProperties {
        /** 是否启用 Testcontainers 自动化管理 */
        private boolean enabled = false;

        /** 是否开启全局容器复用 (需配合 ~/.testcontainers.properties) */
        private boolean reuse = false;

        /** 基础设施服务列表 (MySQL, Redis, Kafka, etc.) */
        private List<ServiceConfig> services = new ArrayList<>();
    }

    @Data
    public static class ServiceConfig {
        /** 服务类型: mysql, redis, kafka, mongodb, postgresql, elasticsearch, localstack */
        private String type;

        /** 容器镜像地址 (可选，不填则使用 Provider 默认值) */
        private String image;

        /** 初始化脚本路径 (主要针对 DB 类型) */
        private String initScript;

        /** 数据库名称 (针对 MySQL/Postgres) */
        private String database;

        /** 针对 Localstack 的子服务列表 (s3, sqs, etc.) */
        private List<String> subServices = new ArrayList<>();

        /** 其他自定义扩展配置 */
        private Map<String, Object> options = new HashMap<>();
    }

    // --- 内部结构类 ---

    @Data
    public static class MockDefinition {
        private String bean;
        private String method;
        private Map<String, Object> args = new LinkedHashMap<>();
        private Object thenReturn;
        private ExceptionDefinition thenThrow;
    }

    @Data
    public static class ExpectDefinition {
        private Object response; // 支持 Map 或具体 POJO
        private ExceptionDefinition exception;
        private List<DatabaseExpectation> database = new ArrayList<>();
    }

    @Data
    public static class ExceptionDefinition {
        private String type;
        private String message;
    }

    @Data
    public static class DatabaseExpectation {
        private String table;
        private String mode = "strict"; // 默认严格匹配
        private List<Map<String, Object>> rows = new ArrayList<>();
    }
}
