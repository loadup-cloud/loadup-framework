import com.github.loadup.components.gateway.common.exception.GatewayException;
import com.github.loadup.components.gateway.message.script.assemble.AbstractMsgAssembler;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class DefaultAssemble extends AbstractMsgAssembler {

    @Override
    public Object assembleMessage(UnifyMsg message) {
        return message.getMessageMap().get("parse_result");
    }

    @Override
    protected Object assembleErrorMessage(UnifyMsg message, GatewayException exception) {
        return "{}";
    }

}