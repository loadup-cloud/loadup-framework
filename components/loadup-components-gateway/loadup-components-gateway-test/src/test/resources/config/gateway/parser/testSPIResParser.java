import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.loadup.components.gateway.core.model.common.MessageEnvelope;
import com.github.loadup.components.gateway.message.script.parser.MessageParser;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class TestParser implements MessageParser {
    @Override
    public UnifyMsg parse(MessageEnvelope messageEnvelope) {
        UnifyMsg message = new UnifyMsg();
        JSONObject jsonObject = JSON.parseObject((String) messageEnvelope.getContent());
        message.getCommonBody().addField("clientQuotes", jsonObject.getString("clientQuotes"));
        message.getCommonBody().addField("result", jsonObject.getString("result"));
        return message;
    }

    public void share(UnifyMsg responseMessage, UnifyMsg requestMessage) {

    }

}