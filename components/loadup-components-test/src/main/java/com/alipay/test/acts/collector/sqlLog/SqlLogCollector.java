/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.alipay.test.acts.collector.sqlLog;

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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alipay.test.acts.model.VirtualTable;

/**
 * SQL log collector
 * 
 * @author chao.gao
 * @author hongling.xiang
 * @version $Id: SqlLogCollector.java, v 0.1 2015-10-26 9:41:48 a.m
 *          hongling.xiang Exp $
 */
public class SqlLogCollector {

    /** logger */
    private static final Logger  logger            = LoggerFactory.getLogger(SqlLogCollector.class);

    /** insert sql regex */
    private static final Pattern insertSqlParttner = Pattern
                                                       .compile("insert([\\s\\S]*) into([\\s\\S]*) values([\\s\\S]*)");

    /** update sql regex */
    private static final Pattern updateSqlParttner = Pattern
                                                       .compile("update([\\s\\S]*) set([\\s\\S]*) where([\\s\\S]*) ");

    /**
     * collect sql logs in specified logfile
     * 
     * <p>
     * reading logfile(avoid any complex computation)
     * </p>
     * 
     * @param sqlLogfileName
     * @return
     * @throws IOException
     */
    public static Map<String, List<List<String>>> collectConcernedSqlLog(String sqlLogfileName,
                                                                         String caseId) {

        Map<String, List<List<String>>> caseSqlLogLines = new HashMap<String, List<List<String>>>();

        BufferedReader logReader = null;
        try {
            logReader = new BufferedReader(new FileReader(sqlLogfileName));

            doCollectSqlLog(sqlLogfileName, logReader, caseSqlLogLines, caseId);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            // close stream
            try {
                if (null != logReader) {
                    logReader.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return caseSqlLogLines;
    }

    /**
     * excute SQL log collect
     * 
     * @param sqlLogfileName
     * @param logReader
     * @param caseSqlLogLines 用来存放<caseId|component,log内容>
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void doCollectSqlLog(String sqlLogfileName, BufferedReader logReader,
                                        Map<String, List<List<String>>> caseSqlLogLines,
                                        String orgCaseId) throws FileNotFoundException, IOException {

        String curLine = StringUtils.EMPTY;
        String caseId = StringUtils.EMPTY;
        List<List<String>> singleCaseSqlLogs = new ArrayList<List<String>>();
        boolean isValiable = false;

        //截取组件Id
        caseId = getLastCaseId(orgCaseId);
        List<List<String>> caseSqlLogs = new ArrayList<List<String>>();
        caseSqlLogLines.put(orgCaseId, caseSqlLogs);

        while ((curLine = logReader.readLine()) != null) {
            // 发现含有caseID就是运行结束了           
            if (StringUtils.contains(curLine, caseId) && StringUtils.contains(curLine, "Finish")
                || (StringUtils.contains(curLine, caseId) && StringUtils.contains(curLine, "组件执行结束"))) {
                //清除便于下次循环使用
                singleCaseSqlLogs.clear();
                if (StringUtils.contains(orgCaseId, "|")) {
                    orgCaseId = orgCaseId.substring(0, orgCaseId.lastIndexOf("|"));
                }

                continue;
            }

            //如果是组件就循环调用
            else if (StringUtils.contains(curLine, "开始执行组件caseId=")) {
                String currentCompentId = curLine.substring(curLine.indexOf("caseId=") + 7,
                    curLine.indexOf(":"));
                caseId = orgCaseId + "|" + currentCompentId;
                doCollectSqlLog(sqlLogfileName, logReader, caseSqlLogLines, caseId);
                singleCaseSqlLogs.clear();
                caseId = orgCaseId;

            }

            else {
                //执行真正的运行sql解析,将当前行及后面的两行.解析之后,放到singleCaseSqlLogs中
                paseExtSql(curLine, logReader, singleCaseSqlLogs);

                if (!CollectionUtils.isEmpty(singleCaseSqlLogs)) {
                    caseSqlLogLines.get(orgCaseId).addAll(singleCaseSqlLogs);
                }
                singleCaseSqlLogs.clear();
            }
        }
    }

    /**
     * 
     * @param orgCaseId
     */
    public static String getFirstCaseId(String orgCaseId) {

        String firstCaseId = StringUtils.EMPTY;
        if (StringUtils.contains(orgCaseId, "|")) {
            String[] strs = orgCaseId.split("\\|");
            firstCaseId = strs[0];
        } else {
            firstCaseId = orgCaseId;
        }

        return firstCaseId;
    }

    /**
     * 获取形如： 被测方法Id||第一个组件Id||第一个嵌套组件ID 这种方式的最后一个ID
     * @param orgCaseId
     */
    public static String getLastCaseId(String orgCaseId) {

        String lastCaseId = StringUtils.EMPTY;
        if (StringUtils.contains(orgCaseId, "|")) {
            String[] strs = orgCaseId.split("\\|");
            lastCaseId = strs[strs.length - 1];
        } else {
            lastCaseId = orgCaseId;
        }

        return lastCaseId;
    }

    /**
     * 执行运行sql解析 
     * @param curLine
     * @param logReader
     * @param singleCaseSqlLogs
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void paseExtSql(String curLine, BufferedReader logReader,
                                  List<List<String>> singleCaseSqlLogs)
                                                                       throws FileNotFoundException,
                                                                       IOException {

        if (StringUtils.contains(curLine, "Executing Statement:")) {
            List<String> singleSqlExecLog = new ArrayList<String>();
            // exec sql line
            singleSqlExecLog.add(curLine);
            // sql param line
            String paramFirstLine = logReader.readLine();

            //复杂类型的数据组装
            String currentLine = logReader.readLine();
            while (currentLine != null) {
                if (!StringUtils.contains(currentLine, "Types:")) {
                    //换行类型的大字段用;号分割，和flag匹配
                    paramFirstLine += ";";
                    paramFirstLine += currentLine;
                    currentLine = logReader.readLine();
                } else {
                    break;
                }
            }
            singleSqlExecLog.add(paramFirstLine);

            // sql param type line
            singleSqlExecLog.add(currentLine);

            //将脚本转换为小写
            singleSqlExecLog = parseToLow(singleSqlExecLog);

            if (isConcernedSql(singleSqlExecLog)) {
                //System.out.println(singleSqlExecLog);
                singleCaseSqlLogs.add(singleSqlExecLog);
            }
        }
    }

    /**
     * 将执行脚本sql中的大些转换为小写，方便后续的匹配
     * @param singleSqlExecLog
     * @return
     */
    public static List<String> parseToLow(List<String> singleSqlExecLog) {
        String execSqlLogLine = singleSqlExecLog.get(0);

        if (execSqlLogLine.contains("INSERT ")) {
            execSqlLogLine = execSqlLogLine.replace("INSERT ", "insert ");
            execSqlLogLine = execSqlLogLine.replace(" INTO", " into");
            execSqlLogLine = execSqlLogLine.replace("VALUES ", "values ");
        }

        if (execSqlLogLine.contains("UPDATE ")) {
            execSqlLogLine = execSqlLogLine.replace("UPDATE ", "update ");
            execSqlLogLine = execSqlLogLine.replace("SET ", "set ");
            execSqlLogLine = execSqlLogLine.replace("WHERE ", "where ");
        }

        singleSqlExecLog.set(0, execSqlLogLine);

        return singleSqlExecLog;
    }

    /**
     * resolve sql log and build VirtualTable object
     * 
     * @param sqlLogLines
     * @return
     */
    public static List<VirtualTable> parseSqlLog(List<List<String>> curCaseSqlLogLines) {

        // thread safe List
        final List<VirtualTable> virtualTableSet = Collections
            .synchronizedList(new ArrayList<VirtualTable>());

        // resolve all tables associated with a single case
        parseSingleCaseSqlLog(curCaseSqlLogLines, virtualTableSet);

        // remove duplicated tables
        exculeRepeatTableData(virtualTableSet);

        return virtualTableSet;
    }

    /**
     * remove duplicated table data
     * 
     * @param virtualTableSet
     */
    private static void exculeRepeatTableData(List<VirtualTable> virtualTableSet) {

        if (CollectionUtils.isEmpty(virtualTableSet)) {
            return;
        }

        Iterator<VirtualTable> tableIters = virtualTableSet.iterator();

        //临时保存原始数据集合
        List<VirtualTable> originalTableSet = new ArrayList<VirtualTable>();
        originalTableSet.addAll(virtualTableSet);

        int index = 0;
        while (tableIters.hasNext()) {
            VirtualTable virtualTable = tableIters.next();
            index++;
            for (int i = index; i < originalTableSet.size(); i++) {
                if (StringUtils.equalsIgnoreCase(virtualTable.getTableName(), originalTableSet
                    .get(i).getTableName())) {
                    List<Map<String, Object>> target = virtualTable.getTableData();
                    List<Map<String, Object>> other = originalTableSet.get(i).getTableData();

                    //若除本对象外其余还有重复数据，则删除本重复数据
                    doExclueRepeatTableData(target, other);

                    //如果targe内容全部重复
                    if (CollectionUtils.isEmpty(target)) {
                        tableIters.remove();
                    }
                    break;
                }
            }

        }

    }

    /**
     * @param tableData
     * @param tableData2
     * @return
     */
    @SuppressWarnings("unchecked")
    private static void doExclueRepeatTableData(List<Map<String, Object>> target,
                                                List<Map<String, Object>> other) {

        Iterator<Map<String, Object>> targetIter = target.iterator();
        while (targetIter.hasNext()) {

            Map<String, Object> orderMapRow = MapUtils.orderedMap(targetIter.next());
            for (Map<String, Object> map : other) {
                Map<String, Object> orderTempRow = MapUtils.orderedMap(map);
                if (StringUtils.equalsIgnoreCase(orderMapRow.toString(), orderTempRow.toString())) {
                    targetIter.remove();
                    break;
                }
            }

        }

    }

    /**
     * 异步线程解析单个用例SQL日志
     * 
     * @param caseSqlLogLines
     * @param caseId
     * @param virtualTableSet
     */
    private static void parseSingleCaseSqlLog(List<List<String>> curCaseSqlLogLines,
                                              final List<VirtualTable> virtualTableSet) {

        for (final List<String> singleSqlExecLog : curCaseSqlLogLines) {
            try {
                // 预处理：去除sql无关的字符串
                preprocessSqlLog(singleSqlExecLog);

                // 解析sql生成VirtualTable
                VirtualTable virtualTable = SqlLogParseFactory.genVirtualTable(
                    getSqlType(singleSqlExecLog.get(0)), singleSqlExecLog);
                if (null != virtualTable) {
                    virtualTableSet.add(virtualTable);
                }

            } catch (Throwable t) {
                //解析当前sql失败则忽略
                logger.warn("Collecting case result-unknown exception while parsing SQL ,SQL="
                            + singleSqlExecLog.get(0), t);
            }
        }

    }

    /**
     * 预处理每行sql日志
     * 
     * @param singleSqlExecLog
     */
    public static void preprocessSqlLog(List<String> singleSqlExecLog) {

        // 去空格
        String execSqlLogLine = singleSqlExecLog.get(0).trim();
        String sqlParamLogLine = singleSqlExecLog.get(1).trim();
        String sqlParamTypeLine = singleSqlExecLog.get(2).trim();

        // --去sql无关的字符
        // 去除执行sql前端无用字符串
        execSqlLogLine = execSqlLogLine
            .substring(execSqlLogLine.indexOf("Executing Statement:") + 20);

        // 仅截取sql参数字符
        sqlParamLogLine = sqlParamLogLine.substring(sqlParamLogLine.indexOf("Parameters:") + 11)
            .trim();
        sqlParamLogLine = sqlParamLogLine.substring(1, sqlParamLogLine.length() - 1);

        // 仅截取sql类型参数字符
        sqlParamTypeLine = sqlParamTypeLine.substring(
            StringUtils.lastIndexOf(sqlParamTypeLine, "Types:") + 6).trim();
        sqlParamTypeLine = sqlParamTypeLine.substring(1, sqlParamTypeLine.length() - 1);

        // 覆盖旧值
        singleSqlExecLog.set(0, execSqlLogLine);
        singleSqlExecLog.set(1, sqlParamLogLine);
        singleSqlExecLog.set(2, sqlParamTypeLine);
    }

    /**
     * 获取sql类型
     * 
     * @param sql
     * @return
     */
    private static String getSqlType(String sql) {

        if (insertSqlParttner.matcher(sql).find()) {
            return SqlTypeEnum.INSERT_SQL.getCode();
        }

        if (updateSqlParttner.matcher(sql).find()) {
            return SqlTypeEnum.UPDATE_SQL.getCode();
        }

        throw new Error("unknown sql type：" + sql);
    }

    public static void main(String[] args) {
        String sqlSelect = "select      id,gmt_create,gmt_modified,gmt_creator,gmt_modifier,app_shop_last_24_query_cnt,approve_last_24_query_cnt,approve_last_12_query_cnt,approve_last_6_query_cnt,approve_last_3_query_cnt,card_last_24_query_cnt,card_last_12_query_cnt,card_last_6_query_cnt,card_last_3_query_cnt,manage_last_24_query_cnt,manage_last_12_query_cnt,manage_last_6_query_cnt,manage_last_3_query_cnt,manage_last_6_query_rate,loan_gt_10000_rate,loan_max_amount,loan_oper_max_amount,loan_cur_ovd_amount,loan_delq_3mth_max,loan_delq_6mth_max,loan_delq_12mth_max,loan_wxh_6_24mth_cur_ovd_pct,scard_delq_3mth_max,scard_delq_6mth_max,scard_delq_12mth_max,decard_max_amount,card_wxh_his_utli_gt_90_cnt,card_cur_ovd_amt_amount,card_delq_3mth_max,card_delq_6mth_max,card_delq_12mth_max,card_delq_3_24mth_max,card_delq_15_24mth_count,card_wxh_cur_ovd_pct,contact_addr_if,marrage_flag,credit_his_year,loan_sum_amount,balance_sum,decard_sum_amount,sixm_avg_used_sum,loan_avg_amount,decard_avg_amount,no_loan_manage_num,normal_num,ovd_24_num,ovd_3_max,avg_util_act_woe,op_assure_loan_amt,house_loan_num,dpd_mth_num,repay_house_mon,repay_car_mon,ln_nca_pmo_avg,repay_else,book_dt,sum_repay_car_loan,sum_repay_house_loan,sum_assure_loan,sum_credit_amt,sum_consumption_amt_gt_100000,sum_consumption_amt_lt_100000,dull_count_credit,class_5_credit_mortgage,ovd_cont_credit_mortgage_m12,class_5_not_mortgage,ovd_cont_not_mortgage_m12,class_5_guarantee,ovd_cont_credit_m12,ovd_cont_loan_m12,cont_under_secondary_assure,approve_last_6_query_cnt_all,query_no,card_delq_13_24mth_max,card_100k_ovd_90pct_cnt,card_rmb_ovd_90pct_cnt,card_gt_100k_cnt,card_60day_unupdate_cnt,card_rmb_cnt,loan_60day_unupdate_cnt,loan_cur_ovd_amt_sum_other,loan_delq_12mth_max_other,loan_asset_class_other,card_last_6_query_org_cnt,card_last_3_query_org_cnt,approve_last_3_query_org_cnt,assure_under_attention_amt,assure_amt_sum,card_amt_sum,loan_ovd_max_bal,card_max_ovd_bal,entity_code,entity_name,compute_date,card_delq_3mth_lt_0_cnt,card_delq_12mth_lt_0_cnt,app_last_30d_query_cnt,app_last_180d_query_cnt,card_delq_3mth_max_cnt,card_delq_12mth_max_cnt,all_card_cur_ovd_amt_sum,all_card_cur_ovd_used,card_open_mth_avg,loan_house_bal_sum,loan_house_amt_sum,loan_delq_12mth_avg_cnt,loan_delq_3mth_avg_cnt,card_nca_hb_max,card_used_50pct_all_card_rate,app_last_90d_query_cnt,card_gt_100k_avg_used_90pct_rate,card_last_30d_query_org_cnt,card_loan_60day_unupdate_cnt,loan_ovd_max_bal_cpt,business_loan_sum_amount,business_loan_sum_balance,cont_under_attention_assure,card_last_90d_query_div_730d_query_cnt_rate,query_flag,cont_all_assure,all_card_cnt,all_loan_cnt,cont_dull_card_cnt,scard_max_ovd_month_cnt,dull_loan_cnt,dull_scard_cnt,car_loan_num,biz_loan_num,std_loan_num,os_house_loan_num,os_car_loan_num,os_biz_loan_num,os_std_loan_num,cust_education,fund_month_amt,pension_pay_base,card_issue_org_cnt,loan_max_ovd_month,card_max_ovd_month,cc_cd_max_24m,ln_cd_max_24m,cc_nca_cnt_sum,ln_nca_m1_max_12m,ln_nca_pmm_avg,ln_nca_rmm_avg,ln_nca_pmt_sum,ac_nca_lmt_avg,ac_nca_cu_avg,work_year,industry,credit_score,credit_prob,credit_lnodds,credit_level,credit_levels,credit_levels_reasons,in_nal_app_cnt_sum,cc_nca_o1w_cnt,cc_nca_cnt,ac_nml_lmt_avg,in_org_cnt_sum_360d,in_org_360d_pct_730d,in_cc_apprate_180d,ln_nca_amt_avg,cc_cnt_sum,cc_act_m1_cnt_3m,cc_m2_cnt_sum_24m_rmb,credit_opm_max,cc_nac_cnt_sum,cc_act_lmt_max,in_lnapp_org_cnt_sum_360d,in_lnapp_org_cnt_sum_730d,ln_bad_crd_cnt_sum,bpboc_iii_score,bpboc_iii_prob,cc_nca_m2_rcy,qc_act_cd_max_24m,as_cv2_bal_sum,os_all_loan_num,ln_nca_rbus_bal_sum,ac_nml_lmt_sum,ln_nca_rmm_min     from   agds_pboc_credit   where       (entity_name=? and    entity_code=?)";

        String sqlUpdate = "update a set b = c where d=e";
        String sqlInsert = "insert into a values(b,c)";

        String type = getSqlType(sqlInsert);
        System.out.print(type);
    }

    /**
     * 仅关注insert、update sql
     * 
     * @param sql
     * @return
     */
    private static boolean isConcernedSql(List<String> singleSqlExecLog) {

        String execSqlLogLine = singleSqlExecLog.get(0);
        String sqlParamLogLine = singleSqlExecLog.get(1);
        String sqlParamTypeLine = singleSqlExecLog.get(2);

        if (StringUtils.isBlank(execSqlLogLine) || StringUtils.isBlank(sqlParamLogLine)
            || StringUtils.isBlank(sqlParamTypeLine)) {
            return false;
        }

        if (!StringUtils.contains(sqlParamLogLine, "Parameters:")) {
            return false;
        }

        if (!StringUtils.contains(sqlParamTypeLine, "Types:")) {
            return false;
        }

        if (insertSqlParttner.matcher(execSqlLogLine.trim()).find()
            || updateSqlParttner.matcher(execSqlLogLine.trim()).find()) {
            return true;
        }

        return false;
    }
}
