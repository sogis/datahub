package ch.so.agi.datahub.auth;

import java.util.UUID;
import org.springframework.security.core.AuthenticatedPrincipal;

public record ApiKeyUser(long organisationId, UUID keyId, String name)
    implements AuthenticatedPrincipal {
  @Override
  public String getName() {
    return name;
  }
}
