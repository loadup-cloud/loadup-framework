/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.testify.utils;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class RandomUtils {

    private static final char[] letters = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z'
    };

    private static final char[] numLetters = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    /**
     * 生成随机randomletters@alitest Email
     *
     * @return
     */
    public static String generateRandomEmail() {
        return "email" + new Date().getTime() + RandomStringUtils.randomNumeric(3) + "@alitest.com";
    }

    /**
     * 生成随机手机号码
     *
     * @return
     */
    public static String generateRandomMobile() {
        return "18" + RandomStringUtils.randomNumeric(9);
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 字符串的长度
     * @return
     */
    public static String generateRandomString(int length) {
        return RandomStringUtils.random(length, letters);
    }

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 字符串的长度
     * @return
     */
    public static String generateRandomNumString(int length) {
        return RandomStringUtils.random(length, numLetters);
    }

    /**
     * 生成身份证
     *
     * @param
     * @return
     */
    public static String generateRandomIdentiyCardNo() {
        // 随机生成省、自治区、直辖市代码 1-2
        String provinces[] = {
            "11", "12", "13", "14", "15", "21", "22", "23", "31", "32", "33", "34", "35", "36",
            "37", "41", "42", "43", "44", "45", "46", "50", "51", "52", "53", "54", "61", "62",
            "63", "64", "65", "71", "81", "82"
        };
        String province = provinces[new Random().nextInt(provinces.length - 1)];
        // 随机生成地级市、盟、自治州代码 3-4
        String citys[] = {
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "21", "22", "23", "24", "25", "26", "27", "28"
        };
        String city = citys[new Random().nextInt(citys.length - 1)];
        // 随机生成县、县级市、区代码 5-6
        String countys[] = {
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "21", "22", "23", "24",
            "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38"
        };
        String county = countys[new Random().nextInt(countys.length - 1)];
        // 随机生成出生年月 7-14
        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) - new Random().nextInt(365 * 100));
        String birth = dft.format(date.getTime());
        // 随机生成顺序号 15-17
        String no = StringUtils.leftPad(new Random().nextInt(999) + "", 3, "0");
        String idCardNumber17 = province + city + county + birth + no;
        // 随机生成校验码 18
        char check = IdentityCardUtils.getOverHeadBit(idCardNumber17 + " ");
        // 拼接身份证号码
        return idCardNumber17 + check;
    }

    /**
     * 根据证件类型(使用MasterDataCertTypeEnum枚举)，身份证是A，港澳是M，其它的不区分
     *
     * @param type A/M/其它任意
     * @return HashMap<String, String>：key有certNo、name
     */
    public static HashMap<String, String> generateRandomCardNo(char type) {
        HashMap<String, String> result = new HashMap<String, String>();
        String name = "";
        String certNo = "";
        switch (type) {
            case 'A':
                name = RandomChinese.getFixedLengthChinese(2);
                certNo = generateRandomIdentiyCardNo();
                break;
            case 'M':
                name = RandomChinese.getFixedLengthChinese(2);
                certNo = generateRandomString(2).toUpperCase() + generateRandomNumString(8);
                break;
            default:
                name = generateRandomString(6);
                certNo = generateRandomString(2).toUpperCase() + generateRandomNumString(8);
                break;
        }
        result.put("name", name);
        result.put("certNo", certNo);
        return result;
    }

    /**
     * 营业执照15位注册号码含义和规则
     *
     * <p>
     *
     * <pre>
     *     格式：6位行政区划+8位顺序位+1位校验位
     *     8位顺序位：1位（企业性质）+ 1位（法人或分支机构标识） +6位顺序位
     *     1位校验位使用支付宝的校验方式生成
     * </pre>
     *
     * <p>
     * 前六位代表的是工商行政管理机关的代码，国家工商行政管理总局用“100000”表示，省级、地市级、区县级登记机关代码分别使用6位行政区划代码表示。
     *
     * <p>
     *
     * <pre>
     *  顺序码是7-14位，顺序码指工商行政管理机关在其管辖范围内按照先后次序为申请登记注册的市场主体所分配的顺序号。
     *  为了便于管理和赋码，8位顺序码中的第1位（自左至右）采用以下分配规则：
     *  1、个体工商户：  第七位数（顺序码第一位，依此类推，下同）全省先统一使用“6”，
     *                即6XXXXXXX。7、8、9三个数字根据发展变化由省局决定启用时间。
     *                剩余号码由县级局按照个体工商户分层分类登记管理的要求，结合本辖区实际情况，科学的细化号码分配，确保辖区内号码的唯一性。
     *  2、内资企业：  第七位数为“0”表示全民所有制企业、集体所有制企业、联营企业、股份合作制企业，
     *                      “1”表示国有独资、法人独资、国有控股、法人投资或控股的有限责任公司和股份有限公司，
     *                       2表示自然人独资、自然人投资或控股的有限责任公司及自然人发起设立的股份有限公司，
     *                       3表示其他非公司企业（包括个人独资企业、合伙企业、农民专业合作社）；
     *               第八位数为“1”表示具有法人资格（含个人独资企业、合伙企业），
     *                       “2”表示营业单位或分支机构（分公司）。
     *               第九位数到第十四位数用顺序号表示。
     *               明细为：“01XXXXXX”表示内资企业法人，
     *                     “02XXXXXX” 表示内资企业法人分支机构以及事业法人所办的营业单位，
     *                     “11XXXXXX” 表示内资有限公司，
     *                     “12XXXXXX” 表示内资有限公司分支机构，
     *                     “21XXXXXX” 表示私营有限公司，
     *                     “22XXXXXX” 表示私营有限公司分支机构，
     *                     “31XXXXXX” 表示其他非公司企业法人、个人独资企业、合伙企业，
     *                     “32XXXXXX” 表示其他非公司企业法人分支机构。
     * </pre>
     *
     * @return
     */
    public static String generateRandomLicenceNo() {
        // 随机生成行政区划 6位
        String capitals[] = {"230100", "110000", "330100", "460200"};
        String capitalCode = capitals[new Random().nextInt(capitals.length)];
        // 随机生成企业性质 第7位
        String natures[] = {"0", "1", "2", "3"};
        String nature = natures[new Random().nextInt(natures.length - 1)];
        // 随机生成法人或分支机构标识 第8位
        String leagalOrBranches[] = {"1", "2"};
        String leagalFlag = leagalOrBranches[new Random().nextInt(leagalOrBranches.length)];
        // 随机生成顺序号 15-17
        String seq = StringUtils.rightPad(new Random().nextInt(999999) + "", 6, "0");
        String licenceNoTemp = capitalCode + nature + leagalFlag + seq;
        // 拼接
        return licenceNoTemp + String.valueOf(computeChecksum(licenceNoTemp));
    }

    /**
     * 计算校验码。
     *
     * <p>
     * 校验码规则: 前十几位做异或，并用10取模。
     *
     * @param string
     * @return
     */
    public static int computeChecksum(String string) {
        // 计算校验码
        int checksum = 0;
        // 计算校验和
        for (int i = 0; i < string.length(); i++) {
            checksum ^= (string.charAt(i) - '0');
        }
        return checksum % 10;
    }

    /**
     * 组织机构代码证
     *
     * @param string
     * @return
     */
    public static String generateRandomOrgCertNo() {
        // 随机生成省、自治区、直辖市代码 1-2
        String baseNumsPair[][] = {
            {"0", "0"},
            {"1", "1"},
            {"2", "2"},
            {"3", "3"},
            {"4", "4"},
            {"5", "5"},
            {"6", "6"},
            {"7", "7"},
            {"8", "8"},
            {"9", "9"},
            {"A", "10"},
            {"B", "11"},
            {"C", "12"},
            {"D", "13"},
            {"E", "14"},
            {"F", "15"},
            {"G", "16"},
            {"H", "17"},
            {"I", "18"},
            {"J", "19"},
            {"K", "20"},
            {"L", "21"},
            {"M", "22"},
            {"N", "23"},
            {"O", "24"},
            {"P", "25"},
            {"Q", "26"},
            {"R", "27"},
            {"S", "28"},
            {"T", "29"},
            {"U", "30"},
            {"V", "31"},
            {"W", "32"},
            {"X", "33"},
            {"Y", "34"},
            {"Z", "35"}
        };
        int factor[] = {3, 7, 9, 10, 5, 8, 4, 2};
        // 计算校验和
        StringBuffer tempBuffer = new StringBuffer();
        int sum = 0;
        for (int i = 0; i < 8; i++) {
            String numPair[] = baseNumsPair[new Random().nextInt(baseNumsPair.length)];
            tempBuffer.append(numPair[0]);
            sum += factor[i] * Integer.valueOf(numPair[1]);
        }

        int checksum = 11 - sum % 11;

        String checkChar = String.valueOf(checksum);
        if (checksum == 10) {
            checkChar = "X";
        } else if (checksum == 11) {
            checkChar = "0";
        }

        return tempBuffer.append("-").append(checkChar).toString();
    }

    /**
     * 金融机构代码
     *
     * @return
     */
    public static String generateRandomFinancialInstNo() {

        String pos12Pair[][] = {
            {"A", "2"},
            {"B", "3"},
            {"C", "5"},
            {"D", "6"},
            {"E", "4"},
            {"F", "8"},
            {"G", "2"},
            {"H", "2"},
            {"Z", "1"}
        };
        // 随机生成行政区划 6位
        String capitals[] = {"230100", "110000", "330100", "460200"};

        StringBuffer buffer = new StringBuffer();

        String pos12[] = pos12Pair[new Random().nextInt(pos12Pair.length - 1)];

        buffer.append(pos12[0]);
        buffer.append(new Random().nextInt(Integer.parseInt(pos12[1]) + 1));
        buffer.append(StringUtils.rightPad(String.valueOf(new Random().nextInt(9999) + 1), 4, '0'));
        buffer.append(capitals[new Random().nextInt(capitals.length)].substring(0, 2));

        buffer.append(StringUtils.rightPad(String.valueOf(new Random().nextInt(99999) + 1), 5, '0'));

        int checkSum = computeChecksum(buffer.toString());
        return buffer.append(checkSum).toString();
    }
}
