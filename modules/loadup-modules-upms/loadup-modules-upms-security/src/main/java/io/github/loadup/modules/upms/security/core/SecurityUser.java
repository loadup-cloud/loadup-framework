package io.github.loadup.modules.upms.security.core;

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
