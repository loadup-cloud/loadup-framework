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
package io.github.loadup.modules.transfer.app.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.transfer.client.dto.TransferTaskDTO;
import io.github.loadup.modules.transfer.domain.model.TransferPage;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps application results without exposing persistence or domain models. */
@Mapper(config = LoadUpMapStructConfig.class)
public interface TransferDTOConverter {
    TransferTaskDTO toDTO(TransferTask value);

    @Mapping(target = "data", source = "records")
    @Mapping(target = "pageInfo.totalCount", source = "total")
    @Mapping(target = "pageInfo.pageIndex", source = "page")
    @Mapping(target = "pageInfo.pageSize", source = "size")
    PageDTO<TransferTaskDTO> toPageDTO(TransferPage<TransferTask> value);
}
