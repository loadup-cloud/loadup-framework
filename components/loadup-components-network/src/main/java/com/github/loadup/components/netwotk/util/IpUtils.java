package com.github.loadup.components.netwotk.util;

/*-
 * #%L
 * loadup-components-network
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

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.slf4j.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class IpUtils {
    private static final Logger logger = LoggerFactory.getLogger(IpUtils.class);
    private static String NOT_FOUND = "0";
    private static IpUtils instance;
    @Resource
    private Searcher ipSearcher;

    /**
     * 国家|区域|省份|城市|ISP
     */
    public static String search(String ip) {
        // 3、查询
        try {
            long sTime = System.nanoTime();
            String region = instance.ipSearcher.search(ip);
            long cost = TimeUnit.NANOSECONDS.toMicros((long) (System.nanoTime() - sTime));
            logger.info("region: {}, ioCount: {}, took: {} μs", region, instance.ipSearcher.getIOCount(), cost);
            return region;
        } catch (Exception e) {
            logger.info("failed to search ip={},err= {}", ip, e.getMessage());
        }
        return null;
    }

    public static String getCountry(String ip) {
        return split(search(ip), 0);
    }

    public static String getRegion(String ip) {
        return split(search(ip), 1);
    }

    public static String getProvince(String ip) {
        return split(search(ip), 2);
    }

    public static String getCity(String ip) {
        return split(search(ip), 3);
    }

    public static String getIsp(String ip) {
        return split(search(ip), 4);
    }

    private static String split(String region, int i) {
        if (StringUtils.isBlank(region)) {
            return null;
        }
        String[] split = region.split("\\|");
        if (split.length <= i) {
            return null;
        }
        if (i <= 0) {
            return split[0];
        }
        String str = split[i];
        if (!StringUtils.equals(str, NOT_FOUND)) {
            return str;
        }
        return split(region, i - 1);
    }

    @PostConstruct
    public void init() {
        instance = this;
    }
}
