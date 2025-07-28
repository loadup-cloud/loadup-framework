package com.github.loadup.components.extension.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BizScenario {

    public final static  String DEFAULT_BIZ_ID   = "#defaultBizCode#";
    public final static  String DEFAULT_USE_CASE = "#defaultUseCase#";
    public final static  String DEFAULT_SCENARIO = "#defaultScenario#";
    private final static String DOT_SEPARATOR    = ".";

    private String bizCode;
    private String useCase;
    private String scenario;

    public String getUniqueIdentity() {
        return bizCode + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
    }

    /**
     * 创建一个只包含 bizCode 的基础场景
     */
    public static BizScenario valueOf(String bizCode) {
        return BizScenario.builder().bizCode(bizCode).build();
    }

    /**
     * 创建包含 bizCode 和 useCase 的场景
     */
    public static BizScenario valueOf(String bizCode, String useCase) {
        return BizScenario.builder().bizCode(bizCode).useCase(useCase).build();
    }

    /**
     * 创建包含所有维度的完整场景
     */
    public static BizScenario valueOf(String bizCode, String useCase, String scenario) {
        return BizScenario.builder().bizCode(bizCode).useCase(useCase).scenario(scenario).build();
    }
}
