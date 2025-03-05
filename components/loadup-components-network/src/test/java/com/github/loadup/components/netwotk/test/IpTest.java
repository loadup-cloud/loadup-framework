package com.github.loadup.components.netwotk.test;

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

import com.github.loadup.components.netwotk.util.IpUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IpTest {
    @Test
    void testSearchIp() {
        String ip = "114.114.114.114";
        String search = IpUtils.search(ip);
        String country = IpUtils.getCountry(ip);
        String province = IpUtils.getProvince(ip);
        String city = IpUtils.getCity(ip);
        String region = IpUtils.getRegion(ip);
        String isp = IpUtils.getIsp(ip);
        Assertions.assertEquals(search, "中国|0|江苏省|南京市|0");
        Assertions.assertEquals(country, "中国");
        Assertions.assertEquals(region, "中国");
        Assertions.assertEquals(province, "江苏省");
        Assertions.assertEquals(city, "南京市");
        Assertions.assertEquals(isp, "南京市");
    }
}
