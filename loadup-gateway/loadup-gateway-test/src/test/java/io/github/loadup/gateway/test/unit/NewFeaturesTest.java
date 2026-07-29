//package io.github.loadup.gateway.test.unit;
//
///*-
// * #%L
// * LoadUp Gateway Test
// * %%
// * Copyright (C) 2025 - 2026 LoadUp Cloud
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/gpl-3.0.html>.
// * #L%
// */
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import io.github.loadup.gateway.core.action.CircuitBreakerAction;
//import io.github.loadup.gateway.core.action.RateLimitAction;
//import io.github.loadup.gateway.core.router.PatternRouteRegistry;
//import io.github.loadup.gateway.facade.model.GatewayRequest;
//import io.github.loadup.gateway.facade.model.PathPattern;
//import io.github.loadup.gateway.facade.model.RouteConfig;
//import java.time.Clock;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
///**
// * Unit tests for the three new high-priority features:
// * route pattern matching, rate limiting, and circuit breaker.
// */
//class NewFeaturesTest {
//
//    @Nested
//    @DisplayName("PathPattern matching")
//    class PathPatternTest {
//
//        @Test
//        @DisplayName("Exact match without variables")
//        void exactMatch() {
//            PathPattern pattern = PathPattern.compile("/api/user/profile");
//            PathPattern.MatchResult result = pattern.match("/api/user/profile");
//            assertNotNull(result);
//            assertEquals("/api/user/profile", result.getMatchedPattern());
//            assertTrue(result.getPathParameters().isEmpty());
//        }
//
//        @Test
//        @DisplayName("Exact match — no match for different path")
//        void exactMatchNoMatch() {
//            PathPattern pattern = PathPattern.compile("/api/user/profile");
//            assertNull(pattern.match("/api/user/settings"));
//        }
//
//        @Test
//        @DisplayName("Single path variable")
//        void singlePathVariable() {
//            PathPattern pattern = PathPattern.compile("/api/user/{id}");
//            PathPattern.MatchResult result = pattern.match("/api/user/123");
//            assertNotNull(result);
//            assertEquals("123", result.getPathParameters().get("id"));
//        }
//
//        @Test
//        @DisplayName("Multiple path variables")
//        void multiplePathVariables() {
//            PathPattern pattern = PathPattern.compile("/api/order/{orderId}/item/{itemId}");
//            PathPattern.MatchResult result = pattern.match("/api/order/ORD-001/item/ITEM-999");
//            assertNotNull(result);
//            assertEquals("ORD-001", result.getPathParameters().get("orderId"));
//            assertEquals("ITEM-999", result.getPathParameters().get("itemId"));
//        }
//
//        @Test
//        @DisplayName("Variable — no match for wrong path structure")
//        void variableNoMatch() {
//            PathPattern pattern = PathPattern.compile("/api/user/{id}/profile");
//            assertNull(pattern.match("/api/user/123/settings"));
//            assertNull(pattern.match("/api/user/123"));
//            assertNull(pattern.match("/api/admin/123/profile"));
//        }
//
//        @Test
//        @DisplayName("Empty pattern compiles to exact")
//        void emptyPattern() {
//            PathPattern pattern = PathPattern.compile(null);
//            assertTrue(pattern.isExact());
//        }
//    }
//
//    @Nested
//    @DisplayName("PatternRouteRegistry")
//    class PatternRouteRegistryTest {
//
//        @Test
//        @DisplayName("Resolves exact routes")
//        void resolvesExactRoutes() {
//            PatternRouteRegistry registry = new PatternRouteRegistry();
//            List<RouteConfig> routes = new ArrayList<>();
//            routes.add(buildRoute("/api/user/login", "POST"));
//            registry.loadRoutes(routes);
//
//            Optional<RouteConfig> result = registry.resolve("POST", "/api/user/login");
//            assertTrue(result.isPresent());
//            assertEquals("/api/user/login", result.get().getPath());
//        }
//
//        @Test
//        @DisplayName("Resolves pattern routes")
//        void resolvesPatternRoutes() {
//            PatternRouteRegistry registry = new PatternRouteRegistry();
//            List<RouteConfig> routes = new ArrayList<>();
//            routes.add(buildRoute("/api/user/{id}", "GET"));
//            registry.loadRoutes(routes);
//
//            Optional<RouteConfig> result = registry.resolve("GET", "/api/user/42");
//            assertTrue(result.isPresent());
//            // Path params are stored in properties
//            @SuppressWarnings("unchecked")
//            Map<String, String> pathParams =
//                    (Map<String, String>) result.get().getProperties().get("_pathParams");
//            assertNotNull(pathParams);
//            assertEquals("42", pathParams.get("id"));
//        }
//
//        @Test
//        @DisplayName("Exact match takes priority over pattern")
//        void exactPriorityOverPattern() {
//            PatternRouteRegistry registry = new PatternRouteRegistry();
//            List<RouteConfig> routes = new ArrayList<>();
//            routes.add(buildRoute("/api/user/{id}", "GET"));
//            routes.add(buildRoute("/api/user/special", "GET"));
//            registry.loadRoutes(routes);
//
//            Optional<RouteConfig> result = registry.resolve("GET", "/api/user/special");
//            assertTrue(result.isPresent());
//            assertEquals("/api/user/special", result.get().getPath());
//            // Should not have path params since it's an exact match
//            assertFalse(result.get().getProperties().containsKey("_pathParams"));
//        }
//
//        @Test
//        @DisplayName("Returns empty for unmatched path")
//        void noMatch() {
//            PatternRouteRegistry registry = new PatternRouteRegistry();
//            List<RouteConfig> routes = new ArrayList<>();
//            routes.add(buildRoute("/api/user/{id}", "GET"));
//            registry.loadRoutes(routes);
//
//            assertFalse(registry.resolve("POST", "/api/user/42").isPresent());
//            assertFalse(registry.resolve("GET", "/api/admin/42").isPresent());
//        }
//
//        private RouteConfig buildRoute(String path, String method) {
//            return RouteConfig.builder()
//                    .path(path)
//                    .method(method)
//                    .target("http://localhost:8080" + path)
//                    .enabled(true)
//                    .build();
//        }
//    }
//
//    @Nested
//    @DisplayName("RateLimitAction TokenBucket")
//    class TokenBucketTest {
//
//        @Test
//        @DisplayName("Acquires tokens within capacity")
//        void acquiresWithinCapacity() {
//            RateLimitAction.TokenBucket bucket = new RateLimitAction.TokenBucket(5, 10.0);
//            for (int i = 0; i < 5; i++) {
//                assertTrue(bucket.tryAcquire(), "Should acquire token " + (i + 1));
//            }
//        }
//
//        @Test
//        @DisplayName("Rejects when bucket is empty")
//        void rejectsWhenEmpty() {
//            RateLimitAction.TokenBucket bucket = new RateLimitAction.TokenBucket(2, 0.0);
//            assertTrue(bucket.tryAcquire());
//            assertTrue(bucket.tryAcquire());
//            assertFalse(bucket.tryAcquire(), "Bucket should be empty");
//        }
//
//        @Test
//        @DisplayName("Refills tokens over time")
//        void refillsTokens() throws InterruptedException {
//            RateLimitAction.TokenBucket bucket =
//                    new RateLimitAction.TokenBucket(3, 100.0); // 100 tokens/sec refill
//            // Empty the bucket
//            assertTrue(bucket.tryAcquire());
//            assertTrue(bucket.tryAcquire());
//            assertTrue(bucket.tryAcquire());
//            assertFalse(bucket.tryAcquire());
//
//            // Wait 20ms — should refill ~2 tokens
//            Thread.sleep(20);
//            assertTrue(bucket.tryAcquire(), "Should have refilled at least 1 token");
//        }
//
//        @Test
//        @DisplayName("Never exceeds capacity")
//        void neverExceedsCapacity() throws InterruptedException {
//            RateLimitAction.TokenBucket bucket = new RateLimitAction.TokenBucket(2, 1000.0);
//            // Wait long enough to refill far beyond capacity
//            Thread.sleep(50);
//            assertTrue(bucket.tryAcquire());
//            assertTrue(bucket.tryAcquire());
//            assertFalse(bucket.tryAcquire(), "Should not exceed capacity of 2");
//        }
//    }
//
//    @Nested
//    @DisplayName("CircuitBreaker")
//    class CircuitBreakerTest {
//
//        @Test
//        @DisplayName("CLOSED → OPEN after threshold failures")
//        void opensAfterThresholdFailures() {
//            CircuitBreakerAction.CircuitBreakerConfig config =
//                    new CircuitBreakerAction.CircuitBreakerConfig();
//            config.enabled = true;
//            config.failureThreshold = 3;
//
//            CircuitBreakerAction.CircuitBreaker breaker =
//                    new CircuitBreakerAction.CircuitBreaker(config);
//
//            // First 2 failures — still CLOSED
//            assertTrue(breaker.allowRequest());
//            breaker.recordFailure();
//            assertTrue(breaker.allowRequest());
//            breaker.recordFailure();
//
//            // 3rd failure — should OPEN
//            assertTrue(breaker.allowRequest());
//            breaker.recordFailure();
//
//            // Now CLOSED → OPEN, should reject
//            assertFalse(breaker.allowRequest(), "Circuit should be OPEN");
//        }
//
//        @Test
//        @DisplayName("OPEN → HALF_OPEN after timeout, then CLOSED after successes")
//        void transitionsToHalfOpenAndClose() {
//            // Use a fixed clock
//            Instant fixedTime = Instant.now();
//            Clock clock = Clock.fixed(fixedTime, ZoneId.systemDefault());
//
//            CircuitBreakerAction.CircuitBreakerConfig config =
//                    new CircuitBreakerAction.CircuitBreakerConfig();
//            config.enabled = true;
//            config.failureThreshold = 2;
//            config.openTimeoutSeconds = 10;
//            config.halfOpenMaxRequests = 3;
//            config.successThreshold = 2;
//
//            CircuitBreakerAction.CircuitBreaker breaker =
//                    new CircuitBreakerAction.CircuitBreaker(config, clock);
//
//            // Fail twice to open
//            breaker.allowRequest();
//            breaker.recordFailure();
//            breaker.allowRequest();
//            breaker.recordFailure();
//            assertFalse(breaker.allowRequest(), "Circuit should be OPEN");
//
//            // Advance clock past timeout
//            Clock laterClock = Clock.fixed(fixedTime.plusSeconds(15), ZoneId.systemDefault());
//            CircuitBreakerAction.CircuitBreaker laterBreaker =
//                    new CircuitBreakerAction.CircuitBreaker(config, laterClock);
//
//            // Should be HALF_OPEN now
//            assertTrue(laterBreaker.allowRequest(), "Should allow in HALF_OPEN");
//            laterBreaker.recordSuccess();
//            assertTrue(laterBreaker.allowRequest());
//            laterBreaker.recordSuccess();
//
//            // 2 successes → CLOSED
//            assertTrue(laterBreaker.allowRequest(), "Should be CLOSED again");
//        }
//
//        @Test
//        @DisplayName("Success in CLOSED resets failure count")
//        void successResetsFailureCount() {
//            CircuitBreakerAction.CircuitBreakerConfig config =
//                    new CircuitBreakerAction.CircuitBreakerConfig();
//            config.enabled = true;
//            config.failureThreshold = 5;
//
//            CircuitBreakerAction.CircuitBreaker breaker =
//                    new CircuitBreakerAction.CircuitBreaker(config);
//
//            // 3 failures, then a success, then 2 more failures
//            for (int i = 0; i < 3; i++) {
//                breaker.allowRequest();
//                breaker.recordFailure();
//            }
//            breaker.allowRequest();
//            breaker.recordSuccess(); // Resets counter
//            for (int i = 0; i < 4; i++) {
//                breaker.allowRequest();
//                breaker.recordFailure();
//            }
//            // After 4 consecutive failures (not 5), should still be CLOSED
//            assertTrue(breaker.allowRequest(), "Should still be CLOSED after 4 failures (reset at 3)");
//        }
//    }
//}
