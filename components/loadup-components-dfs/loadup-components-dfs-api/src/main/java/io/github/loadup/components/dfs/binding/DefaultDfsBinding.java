package io.github.loadup.components.dfs.binding;

/*-
 * #%L
 * Loadup Dfs Components Api
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.components.dfs.binder.DfsBinder;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.framework.api.binding.AbstractBinding;

public class DefaultDfsBinding extends AbstractBinding<DfsBinder<?, DfsBindingCfg>, DfsBindingCfg>
        implements DfsBinding {

    @Override
    public FileMetadata upload(FileUploadRequest request) {
        return getBinder().upload(request);
    }

    @Override
    public FileDownloadResponse download(String fileId) {
        return getBinder().download(fileId);
    }

    @Override
    public boolean delete(String fileId) {
        return getBinder().delete(fileId);
    }

    @Override
    public boolean exists(String fileId) {
        return getBinder().exists(fileId);
    }

    @Override
    public FileMetadata getMetadata(String fileId) {
        return getBinder().getMetadata(fileId);
    }
}
