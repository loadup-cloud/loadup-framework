import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class QueryRateParserException implements MessageParser {

    @Override
    public UnifyMsg parse(MessageEnvelope messageEnvelope) {
        UnifyMsg unifyMsg = new UnifyMsg();
        unifyMsg.addField("parse_error_exception", "custom exception");
        return unifyMsg;
    }

    public void share(UnifyMsg responseMessage, UnifyMsg requestMessage) {

    }

}