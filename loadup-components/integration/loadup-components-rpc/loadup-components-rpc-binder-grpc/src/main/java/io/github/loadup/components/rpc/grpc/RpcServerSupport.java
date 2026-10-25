/*
 * Copyright 2026 LoadUp Cloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.loadup.components.rpc.grpc;

import io.github.loadup.commons.error.CommonException;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import jakarta.validation.Validator;
import java.util.function.Function;

public final class RpcServerSupport {
    public static final Metadata.Key<String> AUTHORIZATION =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> RESULT_CODE =
            Metadata.Key.of("loadup-result-code", Metadata.ASCII_STRING_MARSHALLER);
    private final Validator validator;

    public RpcServerSupport(Validator validator) {
        this.validator = validator;
    }

    public <P, Q, R, S> void invoke(
            P request,
            StreamObserver<S> observer,
            Function<P, Q> decode,
            Function<Q, R> action,
            Function<R, S> encode) {
        try {
            Q command;
            try {
                command = decode.apply(request);
                if (!validator.validate(command).isEmpty()) throw new IllegalArgumentException("Invalid RPC command");
            } catch (RuntimeException invalid) {
                observer.onError(Status.INVALID_ARGUMENT
                        .withDescription("Invalid RPC command")
                        .asRuntimeException());
                return;
            }
            S response = encode.apply(action.apply(command));
            observer.onNext(response);
            observer.onCompleted();
        } catch (CommonException failure) {
            var trailers = new Metadata();
            var code = failure.getResultCode();
            if (code instanceof Enum<?>) trailers.put(RESULT_CODE, code.getCode());
            observer.onError(Status.FAILED_PRECONDITION
                    .withDescription("Business operation rejected")
                    .asRuntimeException(trailers));
        } catch (RuntimeException failure) {
            observer.onError(
                    Status.INTERNAL.withDescription("RPC operation failed").asRuntimeException());
        }
    }

    public static String resultCode(RuntimeException failure) {
        if (failure instanceof StatusRuntimeException status
                && status.getStatus().getCode() == Status.Code.FAILED_PRECONDITION
                && status.getTrailers() != null) {
            String code = status.getTrailers().get(RESULT_CODE);
            return code == null ? "" : code;
        }
        return "";
    }
}
