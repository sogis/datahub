package ch.so.agi.datahub.auth;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ApiKeyUser implements UserDetails {
  private String username;

  private Long organisationId;
  private java.util.UUID keyId;

  private Collection<? extends GrantedAuthority> authorities;

  // private String type = "user";

  public ApiKeyUser(String username) {
    this.username = username;
  }

  public ApiKeyUser(Long organisationId, java.util.UUID keyId, String username) {
    this.username = username;
    this.organisationId = organisationId;
    this.keyId = keyId;
  }

  public Long getOrganisationId() {
    return organisationId;
  }

  public java.util.UUID getKeyId() {
    return keyId;
  }

  public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
    this.authorities = authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username;
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
    return true;
  }
}
