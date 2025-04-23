package com.github.loadup.components.gateway.test.util;

/*-
 * #%L
 * loadup-components-gateway-test
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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class GatewayLiteFileToDBUtil {

    public static void main(String[] args) throws IOException {

        // prod
        String confRoot =
                "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway.sg-prod";
        String apiFileUrl = confRoot + "/OPENAPI_CONF.csv";
        String spiFileUrl = confRoot + "/SPI_CONF.csv";
        String securityFileUrl = confRoot + "/SECURITY_STRATEGY_CONF.csv";

        // sandbox
        //        String apiFileUrl =
        // "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway
        //        .sg-sandbox/OPENAPI_CONF.csv";
        //        String spiFileUrl =
        // "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway
        //        .sg-sandbox/SPI_CONF.csv";
        //        String securityFileUrl =
        // "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway
        //        .sg-sandbox/SECURITY_STRATEGY_CONF.csv";
        // dev
        //        String apiFileUrl = "/Users/luohao/IdeaProjects/local-utils/src/main/resources/OPENAPI_CONF_DEV.csv";
        //        String spiFileUrl =
        // "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway/SPI_CONF_DEV
        //        .csv";
        //        String securityFileUrl =
        // "/Users/luohao/IdeaProjects/lmgatewaylite/app/launch/src/main/resources/config/gateway
        //        /SECURITY_STRATEGY_CONF.csv";

        Map<String, String> templateValue = getTemplateValueFromFile(confRoot);

        resolveApiConf(apiFileUrl, templateValue);
        resolveSpiConf(spiFileUrl, templateValue);
        resolveSecurity(securityFileUrl);
    }

    private static <T> Stream<T> streamOf(T[] obj) {

        if (obj == null) {
            return Stream.empty();
        }

        return Stream.of(obj);
    }

    private static Map<String, String> getTemplateValueFromFile(String confRoot) {

        Map<String, String> templateValue = new HashMap<>();

        String assemblerFolder = confRoot + "/assembler";
        String parserFolder = confRoot + "/parser";
        File assemblerFolderObj = new File(assemblerFolder);
        File parserFolderObj = new File(parserFolder);

        Stream.concat(streamOf(assemblerFolderObj.listFiles()), streamOf(parserFolderObj.listFiles()))
                .forEach(eachFile -> {
                    String fileName = eachFile.getName();
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(eachFile);
                        byte[] buffer = new byte[2048];
                        StringBuilder content = new StringBuilder();
                        while (fileInputStream.read(buffer) != -1) {
                            content.append(new String(buffer));
                        }
                        templateValue.put(fileName, content.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        return templateValue;
    }

    private static void resolveSecurity(String fileUrl) throws IOException {

        List<Map<String, String>> securityConfData = resolveCsv(fileUrl);

        String insertHead = "INSERT INTO gateway.gateway_security\n"
                + "(tenant_id, security_strategy_code, client_id, cert_content, operate_type, cert_type, status, `key_type`, algo_name, "
                + "properties, algo_properties, gmt_valid, gmt_invalid, gmt_create, gmt_modified) VALUES";

        String insertTailTemplate =
                "(NULL, __security_strategy_code__, __client_id__, __cert_content__, __operate_type__, __cert_type__, __status__, "
                        + "__key_type__, __algo_name__, __properties__, __algo_properties__, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, "
                        + "CURRENT_TIMESTAMP)";

        Set<String> exist = new HashSet<>();

        for (Map<String, String> securityConf : securityConfData) {
            StringBuffer insertSql = new StringBuffer(insertHead);

            String replace = insertTailTemplate
                    .replace("__security_strategy_code__", include(securityConf.get("security_strategy_code")))
                    .replace("__client_id__", include(securityConf.get("client_id")))
                    .replace("__cert_content__", include(securityConf.get("security_strategy_key")))
                    .replace("__operate_type__", include(securityConf.get("security_strategy_operate_type")))
                    .replace("__cert_type__", include(securityConf.get("cert_type")))
                    .replace("__status__", include("VALID"))
                    .replace("__key_type__", include(securityConf.get("security_strategy_key_type")))
                    .replace("__algo_name__", include(securityConf.get("security_strategy_algorithm")))
                    .replace("__properties__", include(securityConf.get("cert_properties")))
                    .replace("__algo_properties__", include(securityConf.get("algorithm_properties")));

            insertSql.append(replace).append(";");

            System.out.println("####################################");
            System.out.println(insertSql.toString());
            System.out.println("####################################");
        }
    }

    private static String include(String input) {
        return "'" + input.replaceAll("'", "\\\\'") + "'";
    }

    private static void resolveApiConf(String fileUrl, Map<String, String> templateValue) throws IOException {

        List<Map<String, String>> apiConfData = resolveCsv(fileUrl);

        String insertHead =
                "INSERT INTO gateway_interface (tenant_id, interface_id, interface_name, url, integration_url, "
                        + "security_strategy_code, version, `type`, status, integration_response_parser, integration_request_header_assemble, "
                        + "integration_request_body_assemble, communication_properties, gmt_create, gmt_modified, interface_request_parser, "
                        + "interface_response_body_assemble, interface_response_header_assemble) VALUES ";

        String insertTailTemplate =
                "(__tenantId__, __interfaceId__, __interfaceName__, __url__, __integrationUrl__, __securityStrategyCode__, __version__ "
                        + ", __type__, __status__, __integrationResponseParser__, __integrationRequestHeaderAssemble__, "
                        + "__integrationRequestBodyAssemble__, __communicationProperties__, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "
                        + "__interfaceRequestParser__, __interfaceResponseBodyAssemble__, __interfaceResponseHeaderAssemble__)";

        Set<String> exist = new HashSet<>();

        for (Map<String, String> apiConf : apiConfData) {
            StringBuffer insertSql = new StringBuffer(insertHead);

            String integrationUri = apiConf.get("integration_uri");

            String callPath = integrationUri.substring(integrationUri.indexOf("://") + "://".length());

            String interfaceId = "platform.OPENAPI." + callPath.replaceAll("/", ".") + ".1.0";

            if (apiConf.get("security_strategy_code").equals("OFF")) {
                continue;
            }

            if (!exist.contains(interfaceId)) {
                exist.add(interfaceId);
            } else {
                continue;
            }

            String replace = insertTailTemplate
                    .replace("__tenantId__", "NULL")
                    .replace("__interfaceId__", include(interfaceId))
                    .replace("__interfaceName__", include(interfaceId))
                    .replace("__url__", include(apiConf.get("open_uri")))
                    .replace("__integrationUrl__", include(apiConf.get("integration_uri")))
                    .replace("__securityStrategyCode__", include(apiConf.get("security_strategy_code")))
                    .replace("__version__", include("1.0"))
                    .replace("__type__", include("OPENAPI"))
                    .replace("__status__", include("VALID"))
                    .replace(
                            "__integrationResponseParser__",
                            include(templateValue.getOrDefault(apiConf.get("integration_service_response_parser"), "")))
                    .replace(
                            "__integrationRequestHeaderAssemble__",
                            include(templateValue.getOrDefault(
                                    apiConf.get("integration_service_request_header_assemble"), "")))
                    .replace(
                            "__integrationRequestBodyAssemble__",
                            include(templateValue.getOrDefault(
                                    apiConf.get("integration_service_request_assemble"), "")))
                    .replace(
                            "__communicationProperties__",
                            include(apiConf.getOrDefault("communication_properties", "")))
                    .replace("__interfaceRequestParser__", include(""))
                    .replace("__interfaceResponseBodyAssemble__", include(""))
                    .replace("__interfaceResponseHeaderAssemble__", include(""));

            insertSql.append(replace).append(";");

            System.out.println("####################################");
            System.out.println(insertSql.toString());
            System.out.println("####################################");
        }
    }

    private static void resolveSpiConf(String fileUrl, Map<String, String> templateValue) throws IOException {

        List<Map<String, String>> spiConfData = resolveCsv(fileUrl);

        String insertHead =
                "INSERT INTO gateway_interface (tenant_id, interface_id, interface_name, url, integration_url, "
                        + "security_strategy_code, version, `type`, status, integration_response_parser, integration_request_header_assemble, "
                        + "integration_request_body_assemble, communication_properties, gmt_create, gmt_modified, interface_request_parser, "
                        + "interface_response_body_assemble, interface_response_header_assemble) VALUES ";

        String insertTailTemplate =
                "(__tenantId__, __interfaceId__, __interfaceName__, __url__, __integrationUrl__, __securityStrategyCode__, __version__ "
                        + ", __type__, __status__, __integrationResponseParser__, __integrationRequestHeaderAssemble__, "
                        + "__integrationRequestBodyAssemble__, __communicationProperties__, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, "
                        + "__interfaceRequestParser__, __interfaceResponseBodyAssemble__, __interfaceResponseHeaderAssemble__)";

        Set<String> exist = new HashSet<>();

        for (Map<String, String> spiConf : spiConfData) {
            StringBuffer insertSql = new StringBuffer(insertHead);

            String integrationUri = spiConf.get("integration_uri");

            String callPath = integrationUri.substring(integrationUri.indexOf("://") + "://".length());

            String interfaceId = "platform.SPI." + callPath.replaceAll("/", ".") + ".1.0";

            if (!exist.contains(interfaceId)) {
                exist.add(interfaceId);
            } else {
                continue;
            }

            String replace = insertTailTemplate
                    .replace("__tenantId__", "NULL")
                    .replace("__interfaceId__", include(interfaceId))
                    .replace("__interfaceName__", include(interfaceId))
                    .replace("__url__", include(""))
                    .replace("__integrationUrl__", include(spiConf.get("integration_uri")))
                    .replace("__securityStrategyCode__", include(spiConf.get("security_strategy_code")))
                    .replace("__version__", include("1.0"))
                    .replace("__type__", include("SPI"))
                    .replace("__status__", include("VALID"))
                    .replace(
                            "__integrationResponseParser__",
                            include(templateValue.getOrDefault(spiConf.get("integration_service_response_parser"), "")))
                    .replace(
                            "__integrationRequestHeaderAssemble__",
                            include(templateValue.getOrDefault(
                                    spiConf.get("integration_service_request_header_assemble"), "")))
                    .replace(
                            "__integrationRequestBodyAssemble__",
                            include(templateValue.getOrDefault(
                                    spiConf.get("integration_service_request_assemble"), "")))
                    .replace(
                            "__communicationProperties__",
                            include(spiConf.getOrDefault("communication_properties", "")))
                    .replace("__interfaceRequestParser__", include(""))
                    .replace("__interfaceResponseBodyAssemble__", include(""))
                    .replace("__interfaceResponseHeaderAssemble__", include(""));

            insertSql.append(replace).append(";");

            System.out.println("####################################");
            System.out.println(insertSql.toString());
            System.out.println("####################################");
        }
    }

    private static List<Map<String, String>> resolveCsv(String fileUrl) throws IOException {
        BufferedReader bufferedReader = null;
        try {

            bufferedReader = new BufferedReader(new FileReader(fileUrl));

            String columnDefineLine = bufferedReader.readLine();
            String[] columnNameArr = columnDefineLine.split(",");

            Map<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < columnNameArr.length; i++) {
                columnMap.put(columnNameArr[i], i);
            }

            List<Map<String, String>> confData = new ArrayList<>();

            String lineData = "";

            while ((lineData = bufferedReader.readLine()) != null && !lineData.equals("")) {

                String[] split = lineData.split(",");
                Map<String, String> apiData = columnMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, m -> {
                            if (m.getValue() > split.length - 1) {
                                return "";
                            } else {
                                return split[m.getValue()];
                            }
                        }));

                confData.add(apiData);
            }

            return confData;

        } finally {

            bufferedReader.close();
        }
    }
}
