package com.github.loadup.components.gotone.dataobject;

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

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 通知模板数据对象
 */
@Getter
@Setter
@Table("gotone_notification_template")
public class NotificationTemplateDO extends BaseDO {

    @Id
    @Column("id")
    private String id;

    @Column("template_code")
    private String templateCode;

    @Column("template_name")
    private String templateName;

    @Column("channel")
    private String channel;

    @Column("content")
    private String content;

    @Column("title_template")
    private String titleTemplate;

    @Column("template_type")
    private String templateType;

    @Column("enabled")
    private Boolean enabled;
}

