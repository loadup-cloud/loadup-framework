import com.github.loadup.components.gateway.message.script.parser.AbstractMsgParser;

import java.util.Map;

public class DefaultParser extends AbstractMsgParser {

    @Override
    protected String parseMessage(String message, Map<String, String> httpHeaderInfo) {

        return message;
    }
}
