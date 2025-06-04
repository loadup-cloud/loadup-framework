/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.object.manager;

/*-
 * #%L
 * loadup-components-test
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.github.loadup.components.testify.object.comparer.UnitComparer;
import com.github.loadup.components.testify.object.comparer.impl.AvailableComparer;
import com.github.loadup.components.testify.object.comparer.impl.ConditionComparer;
import com.github.loadup.components.testify.object.comparer.impl.CustomComparer;
import com.github.loadup.components.testify.object.comparer.impl.DateComparer;
import com.github.loadup.components.testify.object.comparer.impl.FileComparer;
import com.github.loadup.components.testify.object.comparer.impl.MapComparer;
import com.github.loadup.components.testify.object.comparer.impl.NoCheckComparer;
import com.github.loadup.components.testify.object.comparer.impl.RegexComparer;
import com.github.loadup.components.testify.object.comparer.impl.SimpleComparer;
import com.github.loadup.components.testify.object.enums.UnitFlagEnum;
import java.util.HashMap;
import java.util.Map;

/**
 * 单元比较器管理器
 *
 *
 *
 */
public class ObjectCompareManager {

    private static Map<UnitFlagEnum, UnitComparer> comparerManager = new HashMap<UnitFlagEnum, UnitComparer>();

    static {
        comparerManager.put(UnitFlagEnum.N, new NoCheckComparer());
        comparerManager.put(UnitFlagEnum.Y, new SimpleComparer());
        comparerManager.put(UnitFlagEnum.D, new DateComparer());
        comparerManager.put(UnitFlagEnum.R, new RegexComparer());
        comparerManager.put(UnitFlagEnum.C, new ConditionComparer());
        comparerManager.put(UnitFlagEnum.A, new AvailableComparer());
        comparerManager.put(UnitFlagEnum.M, new MapComparer());
        comparerManager.put(UnitFlagEnum.F, new FileComparer());
        comparerManager.put(UnitFlagEnum.CUSTOM, new CustomComparer());
    }

    public static Map<UnitFlagEnum, UnitComparer> getComparerManager() {
        return comparerManager;
    }
}
