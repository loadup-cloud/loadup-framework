package io.github.loadup.modules.upms.client.constant;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

/**
 * OAuth 提供商常量
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public final class OAuthProvider {

    private OAuthProvider() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * GitHub
     */
    public static final String GITHUB = "github";

    /**
     * 微信
     */
    public static final String WECHAT = "wechat";

    /**
     * Google
     */
    public static final String GOOGLE = "google";
}
