///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2014 All Rights Reserved.
// */
//package com.alipay.test.acts.hbase;
//
///*-
// * #%L
// * loadup-components-test
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import com.alipay.test.acts.utils.config.ConfigrationFactory;
//
///**
// * HBASE使用客户端
// *
// * @author sunyuxing
// * @version $Id: HBaseClient.java, v 0.1 Jan 23, 2014 2:47:55 PM sunyuxing Exp $
// */
//public class HBaseClient {
//
//
//    private static int poolSize = 20;
//
//    private static String defaultParent = "/hbase-perf";
//
//    private static String defaultQuorum = "10.98.108.25,10.98.108.26,10.98.108.27";
//
//    private static final Logger logger = LoggerFactory.getLogger(HBaseClient.class);
//
//    static {
//        String zookeeperParent = ConfigrationFactory.getConfigration()
//            .getPropertyValue("zookeeperParent");
//        zookeeperParent = StringUtils.isBlank(zookeeperParent) ? defaultParent : zookeeperParent;
//        String zookeeperQuorum = ConfigrationFactory.getConfigration()
//            .getPropertyValue("zookeeperQuorum");
//        zookeeperQuorum = StringUtils.isBlank(zookeeperQuorum) ? defaultQuorum : zookeeperQuorum;
//
//        Map<String, String> conf = new HashMap<String, String>();
//        conf.put("zookeeper.znode.parent", zookeeperParent);
//        conf.put("hbase.zookeeper.quorum", zookeeperQuorum);
//
//        Configuration configuration = HBaseConfiguration.create();
//        for (String key : conf.keySet()) {
//            configuration.set(key, conf.get(key));
//        }
//        pool = new HTablePool(configuration, poolSize);
//    }
//
//    /**
//     * 获取当前执行tablename的HTable
//     *
//     * @return
//     */
//    private static HTableInterface getHtable(String tableName) {
//        HTableInterface hTable = pool.getTable(tableName);
//        return hTable;
//    }
//
//    /**
//     * 查询语句
//     *
//     * @param tableName
//     * @param get
//     * @return
//     */
//    public static Result query(String tableName, Get get) {
//
//        HTableInterface htable = getHtable(tableName);
//        try {
//            Result result = htable.get(get);
//            return result;
//        } catch (Exception e) {
//            logger.error("HBaseClient query exception!", e);
//        }
//        return null;
//    }
//
//    /**
//     * scan 循环
//     *
//     * @param tableName
//     * @param scan
//     * @return
//     */
//    public static ResultScanner scan(String tableName, Scan scan) {
//
//        HTableInterface htable = getHtable(tableName);
//        try {
//            ResultScanner result = htable.getScanner(scan);
//            return result;
//        } catch (Exception e) {
//            logger.error("HBaseClient query exception!", e);
//        }
//        return null;
//    }
//
//    /**
//     * insert操作
//     *
//     * @param tableName
//     * @param put
//     */
//    public static boolean insert(String tableName, Put put) {
//        HTableInterface htable = getHtable(tableName);
//        try {
//            htable.put(put);
//            if (!htable.isAutoFlush()) {
//                htable.flushCommits();
//            }
//            return true;
//        } catch (Exception e) {
//            logger.error("HBaseClient put exception!", e);
//            return false;
//        }
//    }
//
//    /**
//     * insert操作
//     *
//     * @param tableName
//     * @param mPut
//     */
//    public static boolean insert(String tableName, List<Put> mPut) {
//        HTableInterface htable = getHtable(tableName);
//        try {
//            htable.put(mPut);
//            if (!htable.isAutoFlush()) {
//                htable.flushCommits();
//            }
//            return true;
//        } catch (Exception e) {
//            logger.error("HBaseClient put exception!", e);
//            return false;
//        }
//    }
//
//    /**
//     * 删除语句执行
//     *
//     * @param tableName
//     * @param delete
//     * @param delete
//     */
//    public static boolean delete(String tableName, Delete delete) {
//        HTableInterface htable = getHtable(tableName);
//        try {
//            htable.delete(delete);
//            return true;
//        } catch (Exception e) {
//            logger.error("HBaseClient delete exception!", e);
//            return false;
//        }
//    }
//
//}
