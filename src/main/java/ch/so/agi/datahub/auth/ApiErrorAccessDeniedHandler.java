package ch.so.agi.datahub.auth;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.model.ApiError;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiErrorAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ApiErrorAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ServletOutputStream responseStream = response.getOutputStream();
        mapper.writeValue(responseStream, new ApiError(accessDeniedException.getClass().getCanonicalName(),
                accessDeniedException.getMessage(), Instant.now(), request.getRequestURI(), null));
        responseStream.flush();
    }
}
