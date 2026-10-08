package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.config.AppProperties;
import ch.so.agi.datahub.model.ApiError;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

public class RevokeApiKeyFilter extends OncePerRequestFilter {
  private final KeyManagementService keys;
  private final AppProperties p;
  private final JsonMapper mapper;

  public RevokeApiKeyFilter(KeyManagementService keys, AppProperties p, JsonMapper mapper) {
    this.keys = keys;
    this.p = p;
    this.mapper = mapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String key = request.getHeader(p.getApiKeyHeaderName());
    // Intentional revocation based on the detected scheme; preserve the proxy trust contract.
    if (key != null
        && request.getScheme().equalsIgnoreCase("http")
        && !Arrays.asList(p.getHttpWhitelist()).contains(request.getServerName())) {
      keys.revokeInsecure(key);
      response.setStatus(401);
      response.setContentType("application/json");
      mapper.writeValue(
          response.getOutputStream(),
          new ApiError(
              getClass().getCanonicalName(),
              "Possible API key was revoked.",
              Instant.now(),
              request.getRequestURI(),
              null));
      return;
    }
    chain.doFilter(request, response);
  }
}
