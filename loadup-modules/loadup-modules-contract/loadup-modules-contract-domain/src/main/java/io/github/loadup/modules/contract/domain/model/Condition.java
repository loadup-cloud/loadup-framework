/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.domain.model;

import java.util.List;
import java.util.Objects;

/** A bounded, data-only rule tree. A null root is not a valid rule. */
public sealed interface Condition
        permits Condition.All, Condition.Any, Condition.Not, Condition.Exists, Condition.Compare {
    record All(List<Condition> children) implements Condition {
        public All {
            children = List.copyOf(children);
            if (children.size() > 128) throw new IllegalArgumentException("Too many children");
        }
    }

    record Any(List<Condition> children) implements Condition {
        public Any {
            children = List.copyOf(children);
            if (children.isEmpty() || children.size() > 128) throw new IllegalArgumentException("Invalid OR children");
        }
    }

    record Not(Condition child) implements Condition {
        public Not {
            Objects.requireNonNull(child, "child");
        }
    }

    record Exists(String field) implements Condition {
        public Exists {
            field = ContractChecks.code(field);
        }
    }

    record Compare(String field, ComparisonOperator operator, List<TypedValue> expected, String configurationKey)
            implements Condition {
        public Compare {
            field = ContractChecks.code(field);
            Objects.requireNonNull(operator, "operator");
            expected = List.copyOf(expected);
            if (configurationKey != null) {
                configurationKey = ContractChecks.code(configurationKey);
                if (!expected.isEmpty()
                        || operator == ComparisonOperator.IN
                        || operator == ComparisonOperator.NOT_IN
                        || operator == ComparisonOperator.BETWEEN)
                    throw new IllegalArgumentException("Invalid configuration operand");
            } else {
                int size = expected.size();
                if (operator == ComparisonOperator.IN || operator == ComparisonOperator.NOT_IN) {
                    if (size < 1 || size > 100) throw new IllegalArgumentException("Invalid set size");
                } else if (size != (operator == ComparisonOperator.BETWEEN ? 2 : 1)) {
                    throw new IllegalArgumentException("Invalid operand count");
                }
                TypedValue first = expected.getFirst();
                if (expected.stream().anyMatch(v -> v.type() != first.type()))
                    throw new IllegalArgumentException("Mixed operand types");
                if (operator != ComparisonOperator.EQ
                        && operator != ComparisonOperator.NE
                        && operator != ComparisonOperator.IN
                        && operator != ComparisonOperator.NOT_IN
                        && first.type() == ValueType.BOOLEAN)
                    throw new IllegalArgumentException("Boolean ordering unsupported");
                if (operator == ComparisonOperator.BETWEEN
                        && ConditionEvaluator.compare(first, expected.getLast()) > 0) {
                    throw new IllegalArgumentException("Inverted comparison range");
                }
            }
        }
    }

    static Condition always() {
        return new All(List.of());
    }
}
