package com.github.loadup.components.gotone.domain;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import com.github.loadup.commons.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;

/**
 * 通知模板领域对象
 */
@Getter
@Setter
public class NotificationTemplate extends BaseDomain {
    private String  id;
    private String  templateCode;
    private String  templateName;
    private String  channel;
    private String  content;
    private String  titleTemplate;
    private String  templateType;
    private Boolean enabled;
}

