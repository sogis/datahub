package ch.so.agi.datahub.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.cayenne.ObjectContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.so.agi.datahub.service.EmailService;

@WebMvcTest(controllers = ApiKeySecurityTest.TestController.class)
@Import({WebSecurityConfig.class, ApiErrorAuthenticationEntryPoint.class, ApiErrorAccessDeniedHandler.class})
@TestPropertySource(properties = {
        "app.apiKeyHeaderName=X-API-KEY",
        "app.httpWhitelist=localhost",
        "app.adminAccountInit=false",
        "app.createDirectories=false"
})
class ApiKeySecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObjectContext objectContext;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ResourceBundle resourceBundle;

    @MockBean
    private ApiKeyHeaderAuthenticationService apiKeyHeaderAuthenticationService;

    @Test
    void missingApiKeyUsesApiErrorAuthenticationEntryPoint() throws Exception {
        mockMvc.perform(get("/api/keys/test"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Did not find api key header in request."))
                .andExpect(jsonPath("$.path").value("/api/keys/test"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void invalidApiKeyUsesApiErrorAccessDeniedHandler() throws Exception {
        when(apiKeyHeaderAuthenticationService.authenticate(any(ApiKeyHeaderAuthenticationToken.class)))
                .thenReturn(new ApiKeyHeaderAuthenticationToken("invalid"));

        mockMvc.perform(get("/api/keys/test")
                .header("X-API-KEY", "invalid")
                .with(request -> {
                    request.setServerName("localhost");
                    return request;
                }))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.message").value("Forbidden."))
                .andExpect(jsonPath("$.path").value("/api/keys/test"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @RestController
    static class TestController {
        @GetMapping("/api/keys/test")
        String test() {
            return "ok";
        }
    }
}
