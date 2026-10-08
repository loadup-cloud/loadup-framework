package io.github.loadup.components.extension.core;

public record BizScenario(String bizCode, String useCase, String scenario) {

    public static final String DEFAULT_USE_CASE = "#defaultUseCase#";
    public static final String DEFAULT_SCENARIO = "#defaultScenario#";
    private static final String DOT_SEPARATOR = ".";

    public String getUniqueIdentity() {
        return bizCode + DOT_SEPARATOR + useCase + DOT_SEPARATOR + scenario;
    }

    public static BizScenario valueOf(String bizCode) {
        return new BizScenario(bizCode, DEFAULT_USE_CASE, DEFAULT_SCENARIO);
    }

    public static BizScenario valueOf(String bizCode, String useCase) {
        return new BizScenario(bizCode, useCase, DEFAULT_SCENARIO);
    }

    public static BizScenario valueOf(String bizCode, String useCase, String scenario) {
        return new BizScenario(bizCode, useCase, scenario);
    }
}
