/*
 * #%L
 * LoadUp File Infrastructure
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
package io.github.loadup.modules.file.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.modules.file.domain.model.*;

@Table("file_resource_reference")
public class FileReferenceDO extends BaseDO {
    private String fileId;
    private String referenceType;
    private String referenceId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String value) {
        this.fileId = value;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String value) {
        this.referenceType = value;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String value) {
        this.referenceId = value;
    }
}
