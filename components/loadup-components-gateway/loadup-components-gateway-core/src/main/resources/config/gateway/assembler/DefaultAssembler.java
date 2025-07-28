import com.github.loadup.commons.error.CommonException;
import com.github.loadup.components.gateway.message.script.assemble.AbstractMsgAssembler;
import com.github.loadup.components.gateway.message.unimsg.UnifyMsg;

public class DefaultAssembler extends AbstractMsgAssembler {

    @Override
    public Object assembleMessage(UnifyMsg message) {
        return message.getMessageMap().get("parse_result");
    }

    @Override
    protected Object assembleErrorMessage(UnifyMsg message, CommonException exception) {
        return "{}";
    }
}
