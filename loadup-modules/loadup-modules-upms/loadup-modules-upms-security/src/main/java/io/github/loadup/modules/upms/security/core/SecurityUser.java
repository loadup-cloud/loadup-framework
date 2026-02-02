package io.github.loadup.modules.upms.security.core;

/*-
 * #%L
 * Loadup Modules UPMS Security Layer
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

import io.github.loadup.upms.api.dto.AuthUserDTO;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom UserDetails implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Getter
public class SecurityUser implements UserDetails {

  private final AuthUserDTO user;
  private final Set<String> permissions;

  public SecurityUser(AuthUserDTO user, Set<String> permissions) {
    this.user = user;
    this.permissions = permissions;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.actived();
  }

  public String getUserId() {
    return user.getUserId();
  }

  public String getNickname() {
    return user.getNickname();
  }
}
