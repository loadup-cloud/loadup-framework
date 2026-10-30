package io.github.loadup.components.rpc.dubbo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import org.apache.dubbo.rpc.RpcException;
import org.junit.jupiter.api.Test;

class RpcExceptionCodesTest {

    @Test
    void businessFailureKeepsTheDeclaredResultCode() {
        RpcException failure = RpcExceptionCodes.business(new CommonException(CommonResultCodeEnum.UNAUTHENTICATED));

        assertEquals(RpcException.BIZ_EXCEPTION, failure.getCode());
        assertEquals(CommonResultCodeEnum.UNAUTHENTICATED.getCode(), RpcExceptionCodes.resultCode(failure));
    }

    @Test
    void unrelatedAndTransportFailuresHaveNoBusinessCode() {
        assertEquals("", RpcExceptionCodes.resultCode(new IllegalStateException("boom")));
        assertEquals("", RpcExceptionCodes.resultCode(new RpcException(RpcException.NETWORK_EXCEPTION, "down")));
        assertEquals("", RpcExceptionCodes.resultCode(null));
    }

    @Test
    void markerIsFoundThroughTheCauseChain() {
        RpcException wrapped = new RpcException(
                RpcException.BIZ_EXCEPTION,
                "outer",
                RpcExceptionCodes.business(new CommonException(CommonResultCodeEnum.UNAUTHENTICATED)));

        assertEquals(CommonResultCodeEnum.UNAUTHENTICATED.getCode(), RpcExceptionCodes.resultCode(wrapped));
    }

    @Test
    void malformedMarkerIsIgnored() {
        RpcException failure = new RpcException(RpcException.BIZ_EXCEPTION, "loadup-result-code=");

        assertEquals("", RpcExceptionCodes.resultCode(failure));
        assertSame(failure.getMessage(), "loadup-result-code=");
    }
}
