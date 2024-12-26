import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class QueryRateParser implements MessageParser {

    @Override
    public UnifyMsg parse(MessageEnvelope messageEnvelope) {
        UnifyMsg message = new UnifyMsg();
        message.addField("parse_result", messageEnvelope.getContent());
        return message;
    }

    public void share(UnifyMsg responseMessage, UnifyMsg requestMessage) {

    }

}