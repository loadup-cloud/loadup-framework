/*
 * #%L
 * LoadUp Transfer App
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.transfer.app.config;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.transfer.client.error.TransferResultCodeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.modules.transfer")
public class TransferTaskProperties {
    private int dispatchBatchSize = 20;

    public int getDispatchBatchSize() {
        return dispatchBatchSize;
    }

    public void setDispatchBatchSize(int size) {
        if (size < 1 || size > 100)
            throw new CommonException(io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL);
        this.dispatchBatchSize = size;
    }

    private long maxInputBytes = 20L * 1024 * 1024;
    private long maxOutputBytes = 100L * 1024 * 1024;

    public long getMaxInputBytes() {
        return maxInputBytes;
    }

    public void setMaxInputBytes(long maxInputBytes) {
        if (maxInputBytes < 1) throw new CommonException(TransferResultCodeEnum.MAXINPUTBYTES_MUST_BE_POSITIVE);
        this.maxInputBytes = maxInputBytes;
    }

    public long getMaxOutputBytes() {
        return maxOutputBytes;
    }

    public void setMaxOutputBytes(long maxOutputBytes) {
        if (maxOutputBytes < 1) throw new CommonException(TransferResultCodeEnum.MAXOUTPUTBYTES_MUST_BE_POSITIVE);
        this.maxOutputBytes = maxOutputBytes;
    }
}
