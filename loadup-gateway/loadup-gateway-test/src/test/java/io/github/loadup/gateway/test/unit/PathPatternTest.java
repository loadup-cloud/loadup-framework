package io.github.loadup.gateway.test.unit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.gateway.facade.model.PathPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@DisplayName("PathPattern")
class PathPatternTest {

    @Nested
    @DisplayName("compile")
    class Compile {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null/empty pattern produces exact match")
        void nullOrEmptyProducesExact(String input) {
            PathPattern pattern = PathPattern.compile(input);
            assertThat(pattern.isExact()).isTrue();
        }

        @Test
        @DisplayName("exact path without variables")
        void exactPath() {
            PathPattern pattern = PathPattern.compile("/api/user/profile");
            assertThat(pattern.isExact()).isTrue();
            assertThat(pattern.getRawPattern()).isEqualTo("/api/user/profile");
        }

        @Test
        @DisplayName("pattern with single variable")
        void singleVariable() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}");
            assertThat(pattern.isExact()).isFalse();
            assertThat(pattern.getRawPattern()).isEqualTo("/api/user/{id}");
        }

        @Test
        @DisplayName("pattern with multiple variables")
        void multipleVariables() {
            PathPattern pattern = PathPattern.compile("/api/{version}/user/{id}");
            assertThat(pattern.isExact()).isFalse();
        }

        @Test
        @DisplayName("pattern with variable at beginning")
        void variableAtStart() {
            PathPattern pattern = PathPattern.compile("/{tenant}/api/user");
            assertThat(pattern.isExact()).isFalse();
        }

        @Test
        @DisplayName("pattern with variable at end")
        void variableAtEnd() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}");
            assertThat(pattern.isExact()).isFalse();
        }
    }

    @Nested
    @DisplayName("match — exact")
    class ExactMatch {

        @Test
        @DisplayName("exact match returns result with empty params")
        void exactMatch() {
            PathPattern pattern = PathPattern.compile("/api/user/profile");
            PathPattern.MatchResult result = pattern.match("/api/user/profile");
            assertThat(result).isNotNull();
            assertThat(result.getMatchedPattern()).isEqualTo("/api/user/profile");
            assertThat(result.getPathParameters()).isEmpty();
            assertThat(result.hasParameters()).isFalse();
        }

        @Test
        @DisplayName("exact match returns null for different path")
        void exactMatchDifferentPath() {
            PathPattern pattern = PathPattern.compile("/api/user/profile");
            assertThat(pattern.match("/api/user/settings")).isNull();
        }

        @Test
        @DisplayName("exact match returns null for empty request path")
        void emptyRequestPathReturnsNull() {
            PathPattern pattern = PathPattern.compile("/api/user");
            assertThat(pattern.match("")).isNull();
        }

        @Test
        @DisplayName("exact match returns null for null request path")
        void nullRequestPathReturnsNull() {
            PathPattern pattern = PathPattern.compile("/api/user");
            assertThat(pattern.match(null)).isNull();
        }

        @Test
        @DisplayName("exact match is case-sensitive")
        void exactMatchIsCaseSensitive() {
            PathPattern pattern = PathPattern.compile("/api/User/Profile");
            assertThat(pattern.match("/api/user/profile")).isNull();
        }
    }

    @Nested
    @DisplayName("match — pattern with variables")
    class VariableMatch {

        @Test
        @DisplayName("single variable extracts correctly")
        void singleVariable() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}");
            PathPattern.MatchResult result = pattern.match("/api/user/123");
            assertThat(result).isNotNull();
            assertThat(result.getPathParameters()).containsEntry("id", "123");
            assertThat(result.hasParameters()).isTrue();
        }

        @Test
        @DisplayName("multiple variables extract correctly")
        void multipleVariables() {
            PathPattern pattern = PathPattern.compile("/api/order/{orderId}/item/{itemId}");
            PathPattern.MatchResult result = pattern.match("/api/order/ORD-001/item/ITEM-999");
            assertThat(result).isNotNull();
            assertThat(result.getPathParameters())
                    .containsEntry("orderId", "ORD-001")
                    .containsEntry("itemId", "ITEM-999");
        }

        @Test
        @DisplayName("variable value can contain hyphens and dots")
        void variableWithSpecialChars() {
            PathPattern pattern = PathPattern.compile("/api/file/{filename}");
            PathPattern.MatchResult result = pattern.match("/api/file/doc-v1.2.pdf");
            assertThat(result).isNotNull();
            assertThat(result.getPathParameters()).containsEntry("filename", "doc-v1.2.pdf");
        }

        @Test
        @DisplayName("variable does not capture slashes")
        void variableExcludesSlashes() {
            PathPattern pattern = PathPattern.compile("/api/{resource}/detail");
            assertThat(pattern.match("/api/a/b/detail")).isNull();
        }

        @Test
        @DisplayName("no match for wrong static segments")
        void noMatchWrongStaticSegments() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}/profile");
            assertThat(pattern.match("/api/user/123/settings")).isNull();
            assertThat(pattern.match("/api/admin/123/profile")).isNull();
        }

        @Test
        @DisplayName("no match for shorter path")
        void noMatchShorterPath() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}/profile");
            assertThat(pattern.match("/api/user/123")).isNull();
        }

        @Test
        @DisplayName("no match for longer path")
        void noMatchLongerPath() {
            PathPattern pattern = PathPattern.compile("/api/user/{id}");
            assertThat(pattern.match("/api/user/123/extra")).isNull();
        }

        @Test
        @DisplayName("path parameters are unmodifiable")
        void pathParametersAreUnmodifiable() {
            PathPattern pattern = PathPattern.compile("/api/{id}");
            PathPattern.MatchResult result = pattern.match("/api/42");
            assertThat(result.getPathParameters()).isUnmodifiable();
        }
    }

    @Nested
    @DisplayName("match — edge cases")
    class EdgeCases {

        @Test
        @DisplayName("root path exact match")
        void rootPath() {
            PathPattern pattern = PathPattern.compile("/");
            PathPattern.MatchResult result = pattern.match("/");
            assertThat(result).isNotNull();
            assertThat(result.getMatchedPattern()).isEqualTo("/");
            assertThat(result.getPathParameters()).isEmpty();
        }

        @Test
        @DisplayName("trailing slash mismatch")
        void trailingSlash() {
            PathPattern pattern = PathPattern.compile("/api/user");
            assertThat(pattern.match("/api/user/")).isNull();
            assertThat(pattern.match("/api/user")).isNotNull();
        }
    }
}
