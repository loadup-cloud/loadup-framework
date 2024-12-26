package com.github.loadup.components.gateway.core.common;

import java.util.regex.Pattern;

/**
 * 系统常量统一配置
 */
public final class Constant {

    /**
     * component name
     */
    public static final String COMPONENT_NAME = "gateway";

    /**
     * tracer key
     */
    public static final String TRACER_KEY = "traceId";

    /**
     * limit value expire time QPM
     */
    public static final int EXPIRE_TIME_QPM = 5 * 60;

    /**
     * limit value expire time QPS
     */
    public static final int EXPIRE_TIME_QPS = 5;

    /**
     * interface limit num
     */
    public static final String INTERFACE_LIMIT_KEY = "LIMIT_CONN";

    /**
     * default page
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * default page size
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * default interface version
     */
    public static final String DEFAULT_INTERFACE_VERSION = "v1";

    /**
     * default cert status
     */
    public static final String DEFAULT_CERT_STATUS = "Y";

    /**
     * incr value
     */
    public static final int INCR_VAL = 1;

    /**
     * incr default value
     */
    public static final int INCR_DEFAULT_VAL = 0;

    /**
     * The prefix of client limit dimension
     */
    public static final String LIMIT_CLIENT_PREFIX = "client:";

    /**
     * The prefix of tenant limit dimension
     */
    public static final String LIMIT_TENANT_PREFIX = "tenant:";

    /**
     * repository extpoint bizcode
     */
    public static final String REPOSITORY_EXTPOINT_BIZCODE = "repository.extend.point";

    /**
     * 配置分隔符
     */
    public static final Pattern CONFIG_SEPARATOR = Pattern
            .compile("\r*\n+|;");

    /**
     * 值/对分隔符
     */
    public static final String VALUE_SEPARATOR = "=";

    /**
     * 值/对分隔符 ":"
     */
    public static final String VALUE_SEPARATOR_COLON = ":";

    /**
     * KEY分隔符
     */
    public static final char KEY_SAPERATOR = '-';

    /**
     * ,分隔符
     */
    public final static String COMMA_SEPARATOR = ",";

    /**
     * key value de
     */
    public final static String URL_KEY_VALUE_SEPARATOR = "&";

    /**
     * 下划线
     */
    public final static String UNDERSCORE = "_";

    /**
     * uri分隔符
     */
    public final static String URI_SEPARATOR = "://";

    /**
     * path分割符
     */
    public final static String PATH_SEPARATOR = "/";

    /**
     * path连接符
     */
    public final static String PATH_CONJUNCTION = ".";

    /**
     * 默认版本号
     */
    public static final String DEFAULT_VERSION = "v1";

    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";

    /**
     * TEXT
     */
    public static final String TEXT_STRING = "TEXT";

    /**
     * CONNECTION_TIMEOUT
     */
    public static final String CONNECTION_TIMEOUT = "connect_timeout";

    /**
     * READ_TIMEOUT
     */
    public static final String READ_TIMEOUT = "read_timeout";

    /**
     * DEFAULT_CONNECTION_TIMEOUT
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 8000;

    /**
     * DEFAULT_READ_TIMEOUT
     */
    public static final int DEFAULT_READ_TIMEOUT = 8000;

    /**
     * 屏蔽敏感信息后输出的内容
     */
    public static final String SENSITIVE_CONTENT = "[敏感信息已和谐]";

    /**
     * 报文内容太长不打印
     */
    public static final String TOO_LONG_CONTENT = "[报文内容较长不打印]";

    /**
     * 内部系统外部交易编号前缀
     */
    public static final String INNER_SYSTEM_OUT_TRANS_CODE_ID_PREFIX = "ALI";

    /**
     * 工单中心回执的ID
     */
    public static final String TASK_CENTER_CHECKER_ID = "taskCenter";

    /**
     * sequence名称前缀
     */
    public static final String SEQ_PACKAGEID_PREFIX = "SEQ_SGW_PACKAGEID_";

    /**
     * fininflux 的渠道名称，用于匹配通讯配置
     */
    public static final String FININFLUX_CHANNEL_SYSTEM_ID = "FININFLUX";

    /**
     * settlecore的渠道名称
     */
    public static final String SETTLECORE_CHANNEL_SYSTEM_ID = "SETTLECORE";

    /**
     * TR方法名
     */
    public static final String TR_METHOD_NAME = "TR_METHOD_NAME";

    /**
     * TR调用类型
     */
    public static final String TR_METHOD_TYPE = "TR_METHOD_TYPE";

    /**
     * interface 默认version值
     */
    public static final String INTERFACE_DEFAULT_VERSION = "v1";

    /**
     * signature extension class regex for velocity template
     */
    public static final Pattern SIGNATURE_EXTENSION_CLASS_REGEX = Pattern.compile(
            "(([a-z|A-Z|0-9|_])+)\\s*?\\.thirdPartySign\\s*?\\(\\s*?\"(([a-z|A-Z|0-9|\\.|_])+)\"");

    /**
     * 网银回执中保存订单号的字段
     */
    public static final String DEPOSIT_ID = "depositId";

    /**
     * 是否订单交易成功
     */
    public static final String IS_SETTLE_SUCCESS = "isSettleSuccess";

    /**
     * 清算结果
     */
    public static final String SETTLE_STATUS = "settleStatus";

    /**
     * 网银回执中返回给cashier的字段，通过settlecore传过来的Field名称
     */
    public static final String CASHIER_ORDER_ID = "cashierOrderId";

    /**
     * 网银回执中返回给cashier的字段，通过settlecore传过来的Field名称
     */
    public static final String PAY_REQUEST_ID = "payRequestId";

    /**
     * 交易类型编号
     */
    public static final String TRANS_TYPE_ID = "transTypeId";

    /**
     * 渠道系统编号
     */
    public static final String CHANNEL_SYSTEM_ID = "channelSystemId";

    /**
     * 清算api
     */
    public static final String INST_CHANNEL_API = "instChannelApi";

    /**
     * 交易流水号
     */
    public static final String ORDER_NO = "orderNo";

    /**
     * 间连渠道目标机构
     */
    public static final String INST_ID = "instId";

    /**
     * 错误码
     */
    public static final String RESPONSE_CODE = "responseCode";

    /**
     * 错误信息
     */
    public static final String RESPONSE_MESSAGE = "responseMessage";

    /**
     * 调用结果
     */
    public static final String INVOKE_RESULT = "invokeResult";

    /**
     * 调用耗时
     */
    public static final String INVOKE_TIME = "invokeTime";

    /**
     * 调用方系统
     */
    public static final String CALLER_SYSTEM = "callerSystem";

    /**
     * 调用方ip
     */
    public static final String CALLER_IP = "callerIp";

    /**
     * 已经使用令牌数
     */
    public static final String TOKEN_USED = "tokenUsed";

    /**
     * 允许的最大令牌数
     */
    public static final String TOKEN_MAX = "tokenMax";

    /**
     * 各Zone默认的UID
     */
    public static final String DEFAULT_UID = "defaultUid";

    /**
     * client-id is dispatch to the merchant
     */
    public static final String KEY_HTTP_CLIENT_ID = "clientId";

    /**
     * message type
     */
    public static final String MESSAGE_TYPE = "messageType";

    /**
     * error code
     */
    public static final String ERROR_CODE = "errorCode";

    /**
     * supergw做为server时，将请求过来的HttpServletRequest.getQueryString方法取到的内容放到map里面，key为HTTP_QUERY_STRING
     */
    public static final String HTTP_QUERY_STRING = "HTTP_QUERY_STRING";

    /**
     * http的statuscode
     */
    public static final String HTTP_STATUS_CODE = "_httpStatusCode";

    /**
     * 保存http内容的key
     **/
    public static final String HTTP_CONTENT = "_httpContent";

    /**
     * REQUEST BODY的key
     */
    public static final String REQUEST_BODY_KEY   = "LOAD_UP_REQUEST_BODY";
    /**
     * set HTTP_DISPATCH_FLAG=true in request header if this request is resend by RouteClientFilter
     **/
    public static final String HTTP_DISPATCH_FLAG = "HTTP_DISPATCH_FLAG";

    /**
     * 读取超时时间
     */
    public static final String HTTP_TIME_OUT = "httpTimeout";

    /**
     * 连接超时时间
     */
    public static final String HTTP_CONNECT_TIME_OUT = "httpConnectionTimeout";

    /**
     * 从连接池中取不到连接，最大等待时间
     */
    public static final String HTTP_CONNECT_MNG_TIME_OUT = "httpConnectionManagerTimeout";

    /**
     * 每个host对应的最大连接数
     */
    public static final String MAX_CONNECT_PER_HOST = "maxConnectionsPerHost";

    /**
     * 连接池最大的链接数
     */
    public static final String MAX_TOTAL_CONNECT = "maxTotalConnections";

    /**
     * 是否使用TR
     */
    public static final String IS_USE_TR = "useTR";

    /**
     * form url encoded content type
     */
    public static final String FORM_URL_ENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

    /**
     * 是否使用TR
     */
    public static final String PID = "pid";

    /**
     * 是否使用TR
     */
    public static final String TRANS_CODE = "transCode";

    /**
     * 返回码
     */
    public static final String RT_CODE = "rtCode";

    /**
     * 返回信息
     */
    public static final String RT_MESSAGE = "rtMessage";

    /**
     * 内部系统调用超时阈值下限
     */
    public static final int DEFAULT_TIMEOUT_LOWER_LIMIT = 20000;

    /**
     * 关闭所有渠道或交易限流分zone
     */
    public static final String TOKEN_DIVIDED_CLOSE_VALUE = "CLOSE";

    /**
     * QOS商户限流列表字段名
     */
    public static final String QOS_LIMIT_FIELD = "bizIdentity";

    /**
     * QOS限流tair的key后缀
     */
    public static final String QOS_SUFFIX = "Q";

    /**
     * tenant id limit config prefix
     */
    public static final String TENANT_ID_LIMIT_CONFIG_PREFIX = "gateway.rate.limit.tenant_id.max.limit.";

    /**
     * client id limit config prefix
     */
    public static final String CLIENT_ID_LIMIT_CONFIG_PREFIX = "gateway.rate.limit.client_id.max.limit.";

    /**
     * default release limit value
     */
    public static final int DEFAULT_RELEASE_LIMIT_VALUE = -1;

    public static final String TENANT_COMMUNICATION_PROPERTIES_GROUP_URL = "ALL";

    /**
     * 同步实时数据平台报文标志位字段名
     */
    public static final String SYNC_ZDATA_BUS = "syncZDataBus";

    /**
     * 业务类型
     */
    public static final String ZDB_BIZ_TYPE = "zdb_bizType";

    /**
     * 主交易交换代码
     */
    public static final String ZDB_SUB_MAIN_BIZ_TYPE = "zdb_subMainBizType";

    /**
     * 当前交易交换代码
     */
    public static final String ZDB_SUB_BIZ_TYPE = "zdb_subBizType";

    /**
     * 数据唯一id，如fluxId
     */
    public static final String ZDB_DATA_UNIQUEID = "zdb_dataUniqueId";

    /**
     * 清算流水号
     */
    public static final String ZDB_BIZ_SERIAL_NO = "zdb_bizSerialNo";

    /**
     * 未来可能使用的查询流水号
     */
    public static final String ZDB_BIZ_SERIAL_NO2 = "zdb_bizSerialNo2";

    /**
     * 实时数据平台消息TOPIC
     */
    public static final String ARDP_MESSAGE_TOPIC = "TP_G_SUPERGW";

    /**
     * 实时数据平台消息EVENTCODE
     */
    public static final String ARDP_MESSAGE_EVENT_CODE = "EC-supergw-ardp";

    /**
     * 业务类型分隔符，用于分割上游系统名
     */
    public static final String ZDB_BIZ_TYPE_SAPARATOR = "_";

    /**
     * 路由规则为param时，property为default时标示默认接口
     * <p></p>
     * default version when propertiy is "default"
     */
    public static final String PROPERTY_DEFAULT = "default";

    /**
     * properties split flag of "and"
     */
    public static final String SPLIT_FLAG_AND = "AND";

    /**
     * properties split flag of "or"
     */
    public static final String SPLIT_FLAG_OR = "OR";

    /**
     * regex  value flag
     */
    public static final String FLAG_OF_REGEX = "REGEX_";

    /**
     * flag of match all
     */
    public static final String FLAG_OF_MATCH_ALL = "*";

    /**
     * 解析脚本中强制指定客户端渠道id的字段名
     */
    public static final String FORCE_SELECT_CLIENT_CHANNEL_SYSTEM_ID_FIELD_NAME = "forceSelectClientChannelSystemId";

    /**
     * finmng缓存名
     */
    public static final String FINMNG_CACHE_NAME = "finmngCache";

    /**
     * finmng缓存名
     */
    public static final String DB_CACHE_NAME = "dbCache";

    /**
     * 禁用实体
     */
    public static String DDD       = "http://apache.org/xml/features/disallow-doctype-decl";
    /**
     * 异步交易cacheKey
     */
    public static String CACHE_KEY = "gwCacheKey";
    /**
     * ossId名称
     */
    public static String OSS_ID    = "ossId";

    /**
     * loadtest interfaceId postfix
     */
    public static String LOAD_TEST_INTERFACE_POSTFIX = ".shadowtest";

    /**
     * interface version flag in request
     **/
    public static final String INTERFACE_VERSION = "INTERFACE_VERSION";

    /**
     * interface id config in communication properties
     */
    public static final String INTERFACE_ID = "INTERFACE_ID";

    /**
     * intergration response message parser template in communication properties
     */
    public static final String OPENAPI_MSG_PARSER = "OPENAPI_MSG_PARSER";

    /**
     * gateway parse template
     */
    public static final String PARSE_TEMPLATE_NAME_PREFIX = "PARSE_TPL";

    /**
     * intergration service message body assemble template in communication properties
     */
    public static final String MES_BODY_ASSEMBLE = "MES_BODY_ASSEMBLE";

    /**
     * intergration service message header assemble template in communication properties
     */
    public static final String MSG_HEADER_ASSEMBLE = "MSG_HEADER_ASSEMBLE";

    /**
     * interface type in communication properties
     */
    public static final String INTERFACE_TYPE = "INTERFACE_TYPE";

    /**
     * interface type value in communication properties
     */
    public static final String PLUGIN_OPENAPI = "PLUGIN_OPENAPI";

    /**
     * interface version flag in request
     **/
    public static final String ORIGIN_VERSION = "ORIGIN_VERSION";

    public static final String HEAD_ORIGIN_VERSION = "_Header_Origin_Version";

    public static final String TEXT_FORMAT = "UTF-8";

    public static final int BUFFER_SIZE = 1024;

    public static final String REQUEST_URL_PATH = "request.head.integrationUrl";

    public static final String REQUEST_BODY_PATH = "request.body";

    public static final String SUCCESS_CODE = "SYS00000";

    public static final String SUCCESS_CONTENT = "SUCCESS";

    /**
     * 禁用构造函数
     */
    private Constant() {
        // 禁用构造函数
    }

    /**
     * 组装KEY值，默认使用'-'作为分割符号
     */
    public static String getKey(String... inputs) {
        return getKey(KEY_SAPERATOR, inputs);
    }

    /**
     * 组装KEY值
     */
    public static String getKey(char saperator, String... inputs) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            result.append(input);

            if (i != inputs.length - 1) {
                result.append(saperator);
            }
        }

        return result.toString();
    }

}
