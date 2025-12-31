package com.github.loadup.modules.upms.infrastructure.security;

import com.github.loadup.modules.upms.domain.entity.User;
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

  private final User user;
  private final Set<String> permissions;

  public SecurityUser(User user, Set<String> permissions) {
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
    return Boolean.TRUE.equals(user.getAccountNonExpired());
  }

  @Override
  public boolean isAccountNonLocked() {
    return Boolean.TRUE.equals(user.getAccountNonLocked());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return Boolean.TRUE.equals(user.getCredentialsNonExpired());
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  public Long getUserId() {
    return user.getId();
  }

  public String getNickname() {
    return user.getNickname();
  }

  public Long getDeptId() {
    return user.getDeptId();
  }
}
