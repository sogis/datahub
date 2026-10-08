package ch.so.agi.datahub.auth;

import ch.so.agi.datahub.model.ApiError;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiErrorAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final JsonMapper mapper;

  public ApiErrorAuthenticationEntryPoint(JsonMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    ServletOutputStream responseStream = response.getOutputStream();
    mapper.writeValue(
        responseStream,
        new ApiError(
            authException.getClass().getCanonicalName(),
            authException.getMessage(),
            Instant.now(),
            request.getRequestURI(),
            null));
    responseStream.flush();
  }
}
