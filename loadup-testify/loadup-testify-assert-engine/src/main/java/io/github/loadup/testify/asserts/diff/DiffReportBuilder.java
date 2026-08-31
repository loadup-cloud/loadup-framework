package io.github.loadup.testify.asserts.diff;

/*-
 * #%L
 * Testify Assert Engine
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

import io.github.loadup.testify.asserts.model.FieldDiff;
import java.util.List;

public class DiffReportBuilder {

    // 适配 IDEA 控制台默认宽度，设定为 120 字符
    private static final int TOTAL_WIDTH = 120;
    private static final int COL_PATH = 30;
    private static final int COL_EXP = 25;
    private static final int COL_ACT = 25;
    private static final int COL_MSG = 30;

    public static String build(String scenario, List<FieldDiff> diffs) {
        StringBuilder sb = new StringBuilder();

        // 头部装饰
        sb.append("\n").append("=".repeat(TOTAL_WIDTH)).append("\n");
        sb.append(String.format("  ❌ Assertion Failed | scenario: %s\n", scenario));
        sb.append("=".repeat(TOTAL_WIDTH)).append("\n");

        // 表头
        String headerFormat = "| %-"
                + (COL_PATH - 2)
                + "s | %-"
                + (COL_EXP - 2)
                + "s | %-"
                + (COL_ACT - 2)
                + "s | %-"
                + (COL_MSG - 2)
                + "s |\n";
        sb.append(String.format(headerFormat, "Field Path", "Expected", "Actual", "Message"));
        sb.append("-".repeat(TOTAL_WIDTH)).append("\n");

        // 数据行
        for (FieldDiff diff : diffs) {
            sb.append(String.format(
                    headerFormat,
                    // 路径：保留尾部 (TAIL)
                    truncate(diff.path(), COL_PATH - 2, TruncateMode.TAIL),
                    // 期望/实际：保留头部 (HEAD) 或 中间截断
                    truncate(formatValue(diff.expected()), COL_EXP - 2, TruncateMode.MIDDLE),
                    truncate(formatValue(diff.actual()), COL_ACT - 2, TruncateMode.MIDDLE),
                    // 错误消息：保留头部
                    truncate(diff.message(), COL_MSG - 2, TruncateMode.HEAD)));
        }
        sb.append("-".repeat(TOTAL_WIDTH)).append("\n");
        sb.append(String.format("  Total Diffs: %d\n", diffs.size()));
        sb.append("=".repeat(TOTAL_WIDTH)).append("\n");

        return sb.toString();
    }

    private static String formatValue(Object val) {
        if (val == null) {
            return "null";
        }
        // 如果是日期、对象等，可以在这里特殊处理
        return val.toString().replace("\n", " ").replace("\r", "");
    }

    private static String truncate(String str, int max, TruncateMode mode) {
        if (str == null) {
            return "";
        }
        if (str.length() <= max) {
            return str;
        }

        return switch (mode) {
            // 模式 1：保留头部 "...ta.user_id"
            case TAIL -> "..." + str.substring(str.length() - (max - 3));

            // 模式 2：保留头尾 "eyJhb...GciOi"
            case MIDDLE -> {
                int side = (max - 3) / 2;
                yield str.substring(0, side) + "..." + str.substring(str.length() - side);
            }

            // 模式 3：默认保留头部 "Failed to lo..."
            case HEAD -> str.substring(0, max - 3) + "...";
        };
    }

    public enum TruncateMode {
        HEAD,
        TAIL,
        MIDDLE
    }
}
