/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.util;

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

import java.util.HashMap;
import java.util.Map;

import com.alipay.test.acts.callback.GenerateCacheDataCallBack;

/**
 * 本地内存缓存工具类
 * 
 * @author hongling.xiang
 * @version $Id: LocalCacheUtil.java, v 0.1 2015年10月20日 下午5:54:07 hongling.xiang Exp $
 */
public class LocalCacheUtil {

    /** 本地缓存对象 */
    private static final Map<String, Object> cacheObjects = new HashMap<String, Object>();

    /**
     * 获取key对应的缓存对象
     * 
     * @param key        查询key
     * @param callback 回调接口
     * @return
     */
    public static Object getObject(String key, GenerateCacheDataCallBack callback) {

        //首先直接从缓存中获取值
        Object cacheObject = cacheObjects.get(key);
        if (null != cacheObject) {
            return cacheObject;
        }

        //从缓存中取不到值，则通过其他方式获取值，然后在将新取出的值放到缓存中，并发控制
        Object newCacheObject = null;
        synchronized (new Object()) {
            //重新获取缓存中的值。此处重新获取的原因，是避免并发等待时，缓存中的值被更新
            newCacheObject = cacheObjects.get(key);

            if (null == cacheObject) {
                //重新获取值
                newCacheObject = callback.getCacheValue(key);
                //将取出的值存放至缓存中
                cacheObjects.put(key, newCacheObject);
            }
        }
        return newCacheObject;
    }

    /**
     * 移除指定key的对象
     * 
     * @param key
     */
    public static void remove(String key) {
        cacheObjects.remove(key);
    }

    /**
     * 缓存对象
     * 
     * @param key
     */
    public static void cacheObject(String key, Object obj) {
        cacheObjects.put(key, obj);
    }
}
