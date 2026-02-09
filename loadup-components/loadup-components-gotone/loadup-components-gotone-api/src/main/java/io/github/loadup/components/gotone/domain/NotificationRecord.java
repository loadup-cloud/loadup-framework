package io.github.loadup.components.gotone.domain;

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

import io.github.loadup.commons.domain.BaseDomain;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** 通知记录领域对象 */
@Getter
@Setter
public class NotificationRecord extends BaseDomain {
    private String id;
    private String traceId;
    private String bizCode;
    private String bizId;
    private String messageId;
    private String channel;
    private List<String> receivers;
    private String templateCode;
    private String title;
    private String content;
    private String provider;
    private String status;
    private Integer retryCount;
    private Integer priority;
    private String errorMessage;
    private LocalDateTime sendTime;
}
