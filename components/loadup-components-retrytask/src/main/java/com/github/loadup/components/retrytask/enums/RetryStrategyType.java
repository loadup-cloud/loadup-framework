package com.github.loadup.components.retrytask.enums;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.commons.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The type of retry strategy
 */
@Getter
@AllArgsConstructor
public enum RetryStrategyType implements IEnum {

    /**
     * User can define interval
     * https://www.jianshu.com/p/a289dde63043
     */
    INTERVAL_SEQUENCE("INTERVAL_SEQUENCE", "User can define interval"),
    /**
     * ExponentialWaitStrategy
     * 指数等待策略
     * 第一次失败后，依次等待时长：2^1 * 100;2^2 * 100；2^3 * 100;...
     * http://en.wikipedia.org/wiki/Exponential_backoff
     * ExponentialWaitStrategy
     */
    EXPONENTIAL_WAIT_STRATEGY("EXPONENTIAL_WAIT_STRATEGY", "User can define interval"),
    /**
     * FibonacciWaitStrategy
     * 斐波那契等待策略
     * 第一次失败后，依次等待时长：1*100;1*100；2*100；3*100；5*100；...
     */
    FIBONACCI_WAIT_STRATEGY("FIBONACCI_WAIT_STRATEGY", "User can define interval"),
    /**
     * FixedWaitStrategy
     * 固定时长等待策略
     */
    FIXED_WAIT_STRATEGY("FIXED_WAIT_STRATEGY", "User can define interval"),

    /**
     * RandomWaitStrategy
     * 随机时长等待策略
     */
    RANDOM_WAIT_STRATEGY("RANDOM_WAIT_STRATEGY", "User can define interval"),
    /**
     * IncrementingWaitStrategy
     * 递增等待策略
     * 第一次失败后，将依次等待1s；6s(1+5)；11(1+5+5)s；16(1+5+5+5)s；...
     */
    INCREMENTING_WAIT_STRATEGY("INCREMENTING_WAIT_STRATEGY", "User can define interval"),

    ;

    /**
     * code
     */
    private String code;

    /**
     * description
     */
    private String description;

}
