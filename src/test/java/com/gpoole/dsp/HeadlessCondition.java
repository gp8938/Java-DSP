package com.gpoole.dsp;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class HeadlessCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (Boolean.getBoolean("java.awt.headless") || "true".equals(System.getenv("CI"))) {
            return ConditionEvaluationResult.disabled("Running in headless/CI mode");
        }
        return ConditionEvaluationResult.enabled("Not headless");
    }
}
