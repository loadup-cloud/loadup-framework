import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class QueryRateParserFail implements MessageParser {

    @Override
    public UnifyMsg parse(MessageEnvelope messageEnvelope) {
        throw new Exception();
        return message;
    }

    public void share(UnifyMsg responseMessage, UnifyMsg requestMessage) {

    }

}