///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2021 All Rights Reserved.
// */
//package com.alipay.acts.utils.database;
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
//import com.alibaba.common.lang.ExceptionUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.hbase.HBaseConfiguration;
//import org.apache.hadoop.hbase.HConstants;
//import org.apache.hadoop.hbase.client.*;
//import org.apache.hadoop.hbase.util.Bytes;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.NavigableMap;
//
///**
// * @author jinsai.chen
// * @version : HbaseUtils.java, v 0.1 2021年03月03日 上午11:55 jinsai.chen Exp $
// */
//public class HbaseUtils {
//
//    private static Configuration conf;
//
//    private static String quorum
//            = "hbasetest01.et2sqa.tbsite.net,hbasetest02.et2sqa.tbsite.net,hbasetest03.et2sqa.tbsite.net,hbasetestmaster1.et2sqa.tbsite"
//            + ".net,hbasetestmaster2.et2sqa.tbsite.net";
//
//    private static String znodeParent = "/hbase-et2sqa-perf";
//
//    private static String port = "2181";
//
//    private static Map<String, HTable> tableMap = new HashMap<String, HTable>();
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(HbaseUtils.class);
//
//    static {
//        try {
//            conf = HBaseConfiguration.create();
//            conf.set(HConstants.ZOOKEEPER_QUORUM, quorum);
//            conf.set(HConstants.ZOOKEEPER_ZNODE_PARENT, znodeParent);
//            conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, port);
//            conf.set("hbase.zookeeper.watcher.sync.connected.wait", "30000");
//        } catch (Exception e) {
//            LOGGER.warn(ExceptionUtil.getStackTrace(e));
//        }
//    }
//
//    private static HTable getTable(String tableName) {
//        if (tableMap.containsKey(tableName)) {
//            return tableMap.get(tableName);
//        } else {
//            try {
//                HTable table = new HTable(conf, tableName);
//                tableMap.put(tableName, table);
//                return table;
//            } catch (Exception e) {
//                LOGGER.warn(ExceptionUtil.getStackTrace(e));
//            }
//        }
//        return null;
//    }
//
//    public static boolean upsert(String tableName, String key, Map<String, byte[]> dataList) {
//        try {
//            Put put = new Put(Bytes.toBytes(key));
//            for (Entry<String, byte[]> data : dataList.entrySet()) {
//                String family = StringUtils.substringBefore(data.getKey(), ":");
//                String columnReal = StringUtils.substringAfter(data.getKey(), ":");
//                put.add(Bytes.toBytes(family), Bytes.toBytes(columnReal), data.getValue());
//            }
//            HTable table = getTable(tableName);
//            table.put(put);
//            return true;
//        } catch (Exception e) {
//            LOGGER.warn(ExceptionUtil.getStackTrace(e));
//        }
//        return false;
//    }
//
//    public static boolean delete(String tableName, String key) {
//        try {
//            Delete delete = new Delete(Bytes.toBytes(key));
//            HTable table = getTable(tableName);
//            table.delete(delete);
//            return true;
//        } catch (Exception e) {
//            LOGGER.warn(ExceptionUtil.getStackTrace(e));
//        }
//        return false;
//    }
//
//    public static Map<String, byte[]> query(String tableName, String key) {
//        try {
//            Get get = new Get(Bytes.toBytes(key));
//            HTable table = getTable(tableName);
//            Result queryResult = table.get(get);
//            if (queryResult != null) {
//                return convertRow(queryResult.getMap());
//            }
//        } catch (Exception e) {
//            LOGGER.warn(ExceptionUtil.getStackTrace(e));
//        }
//        return null;
//    }
//
//    private static Map<String, byte[]> convertRow(NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> familyMap) {
//        Map<String, byte[]> result = new HashMap<String, byte[]>();
//        if (familyMap == null || familyMap.entrySet() == null) {
//            return result;
//        }
//        for (Entry<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> familyEntry : familyMap
//                .entrySet()) {
//            String family = Bytes.toStringBinary(familyEntry.getKey());
//            for (Entry<byte[], NavigableMap<Long, byte[]>> qualifierEntry : familyEntry
//                    .getValue().entrySet()) {
//                byte[] value = qualifierEntry.getValue().firstEntry().getValue();
//                result.put(family + ":" + Bytes.toStringBinary(qualifierEntry.getKey()), value);
//            }
//        }
//        return result;
//    }
//
//    /**
//     * Getter method for property <tt>quorum</tt>.
//     *
//     * @return property value of quorum
//     */
//    public static String getQuorum() {
//        return quorum;
//    }
//
//    /**
//     * Setter method for property <tt>quorum</tt>.
//     *
//     * @param quorum value to be assigned to property quorum
//     */
//    public static void setQuorum(String quorum) {
//        HbaseUtils.quorum = quorum;
//    }
//
//    /**
//     * Getter method for property <tt>znodeParent</tt>.
//     *
//     * @return property value of znodeParent
//     */
//    public static String getZnodeParent() {
//        return znodeParent;
//    }
//
//    /**
//     * Setter method for property <tt>znodeParent</tt>.
//     *
//     * @param znodeParent value to be assigned to property znodeParent
//     */
//    public static void setZnodeParent(String znodeParent) {
//        HbaseUtils.znodeParent = znodeParent;
//    }
//
//    /**
//     * Getter method for property <tt>port</tt>.
//     *
//     * @return property value of port
//     */
//    public static String getPort() {
//        return port;
//    }
//
//    /**
//     * Setter method for property <tt>port</tt>.
//     *
//     * @param port value to be assigned to property port
//     */
//    public static void setPort(String port) {
//        HbaseUtils.port = port;
//    }
//}
