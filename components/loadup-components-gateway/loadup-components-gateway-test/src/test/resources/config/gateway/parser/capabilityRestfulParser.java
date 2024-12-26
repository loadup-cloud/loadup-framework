import com.github.loadup.components.gateway.message.script.parser.AbstractMsgParser;

import java.util.Map;

/**
 * 1. provide the defalut method
 * a. parseWithJson(String str):
 * parse the string to json format, and let you can get the data from the json object
 */
public class CapabilityRestfulParser extends AbstractMsgParser {
    @Override
    protected String parseMessage(String message, Map<String, String> httpHeaderInfo) {

        return message;
    }
}