package ch.so.agi.datahub.auth;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.so.agi.datahub.cayenne.CoreApikey;
import ch.so.agi.datahub.cayenne.CoreOrganisation;
import ch.so.agi.datahub.model.ApiError;
import ch.so.agi.datahub.service.EmailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Component
public class RevokeApiKeyFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectContext objectContext;
    
    PasswordEncoder encoder;
    
    ObjectMapper mapper;
        
    EmailService emailService;
    
    final String headerName;
    
    String[] httpWhitelistArray;
    
    ResourceBundle resourceBundle;
    
    public RevokeApiKeyFilter(final String headerName, ObjectContext objectContext, PasswordEncoder encoder,
            EmailService emailService, ObjectMapper mapper, String[] httpWhitelistArray, ResourceBundle resourceBundle) {
        this.headerName = headerName;
        this.objectContext = objectContext;
        this.encoder = encoder;
        this.emailService = emailService;
        this.mapper = mapper;
        this.httpWhitelistArray = httpWhitelistArray;
        this.resourceBundle = resourceBundle;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String host = ServletUriComponentsBuilder.fromCurrentRequest().build().getHost();
        String proto = ServletUriComponentsBuilder.fromCurrentRequest().build().getScheme();
        String key = request.getHeader(headerName);
        
        List<String> httpWhitelist = Arrays.asList(httpWhitelistArray);
        
        if (key != null) {
            if (proto.equalsIgnoreCase("http") && !httpWhitelist.contains(host)) {                
                CoreApikey myApiKey = findApiKeyByHeader(key);

                if (myApiKey != null) {
                    myApiKey.setRevokedat(LocalDateTime.now());
                    objectContext.commitChanges();                    
                    try {
                        CoreOrganisation coreOrganisation = myApiKey.getCoreOrganisation();
                        emailService.send(coreOrganisation.getEmail(), resourceBundle.getString("revokeApiKeyEmailSubject"), resourceBundle.getString("revokeApiKeyEmailBody"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());                        
                    }
                }

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                ServletOutputStream responseStream = response.getOutputStream();
                mapper.writeValue(responseStream, new ApiError(this.getClass().getCanonicalName(),
                        "Possible API key was revoked.", Instant.now(), request.getRequestURI(), null));
                responseStream.flush();
                return;
            }            
        }

        filterChain.doFilter(request, response);
    }

    private CoreApikey findApiKeyByHeader(String key) {
        ApiKeyFormat.ApiKeyParts apiKeyParts = ApiKeyFormat.parseApiKey(key).orElse(null);
        if (apiKeyParts != null) {
            CoreApikey apiKey = ObjectSelect.query(CoreApikey.class)
                    .where(CoreApikey.APIKEY.like(apiKeyParts.keyId() + ":%"))
                    .and(CoreApikey.REVOKEDAT.isNull())
                    .and(CoreApikey.DATEOFEXPIRY.gt(LocalDateTime.now()).orExp(CoreApikey.DATEOFEXPIRY.isNull()))
                    .selectOne(objectContext);

            if (apiKey != null && matchesStoredKey(apiKeyParts, apiKey)) {
                return apiKey;
            }

            return null;
        }

        List<CoreApikey> apiKeys = ObjectSelect.query(CoreApikey.class)
                .where(CoreApikey.REVOKEDAT.isNull())
                .and(CoreApikey.DATEOFEXPIRY.gt(LocalDateTime.now()).orExp(CoreApikey.DATEOFEXPIRY.isNull()))
                .select(objectContext);

        for (CoreApikey apiKey : apiKeys) {
            if (ApiKeyFormat.parseStoredValue(apiKey.getApikey()).isPresent()) {
                continue;
            }
            if (encoder.matches(key, apiKey.getApikey())) {
                return apiKey;
            }
        }

        return null;
    }

    private boolean matchesStoredKey(ApiKeyFormat.ApiKeyParts apiKeyParts, CoreApikey apiKey) {
        return ApiKeyFormat.parseStoredValue(apiKey.getApikey())
                .filter(stored -> stored.keyId().equals(apiKeyParts.keyId()))
                .map(stored -> encoder.matches(apiKeyParts.secret().toString(), stored.hashedSecret()))
                .orElse(false);
    }
}
