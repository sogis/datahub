package ch.so.agi.datahub.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ch.so.agi.datahub.cayenne.CoreApikey;

@Service
public class ApiKeyHeaderAuthenticationService {
    
    private ObjectContext objectContext;
    
    private PasswordEncoder encoder;

    public ApiKeyHeaderAuthenticationService(ObjectContext objectContext, PasswordEncoder encoder) {
        this.objectContext = objectContext;
        this.encoder = encoder;
    }
    
    public ApiKeyHeaderAuthenticationToken authenticate(ApiKeyHeaderAuthenticationToken apiKeyAuthenticationToken) {        
        Optional<ApiKeyFormat.ApiKeyParts> apiKeyParts = ApiKeyFormat.parseApiKey(apiKeyAuthenticationToken.getApiKey());
        if (apiKeyParts.isEmpty()) {
            return apiKeyAuthenticationToken;
        }

        Optional<CoreApikey> apiKey = loadActiveApiKeyById(apiKeyParts.get().keyId());
        if (apiKey.isPresent() && matchesStoredKey(apiKeyParts.get(), apiKey.get())) {
            return authenticate(apiKeyAuthenticationToken, apiKey.get());
        }

        return apiKeyAuthenticationToken;
    }

    protected Optional<CoreApikey> loadActiveApiKeyById(UUID keyId) {
        CoreApikey apiKey = ObjectSelect.query(CoreApikey.class)
                .where(CoreApikey.APIKEY.like(keyId + ":%"))
                .and(CoreApikey.REVOKEDAT.isNull())
                .and(CoreApikey.DATEOFEXPIRY.gt(LocalDateTime.now()).orExp(CoreApikey.DATEOFEXPIRY.isNull()))
                .selectOne(objectContext);

        return Optional.ofNullable(apiKey);
    }

    private ApiKeyHeaderAuthenticationToken authenticate(ApiKeyHeaderAuthenticationToken apiKeyAuthenticationToken, CoreApikey apiKey) {
        ApiKeyUser authenticatedUser = new ApiKeyUser(apiKey.getCoreOrganisation().getAname());
        
        List<GrantedAuthority> grants = new ArrayList<GrantedAuthority>();
        grants.add(new SimpleGrantedAuthority(apiKey.getCoreOrganisation().getArole()));
        authenticatedUser.setAuthorities(grants);
        
        return new ApiKeyHeaderAuthenticationToken(apiKeyAuthenticationToken.getApiKey(), authenticatedUser);
    }

    private boolean matchesStoredKey(ApiKeyFormat.ApiKeyParts apiKeyParts, CoreApikey apiKey) {
        Optional<ApiKeyFormat.StoredApiKeyParts> storedParts = ApiKeyFormat.parseStoredValue(apiKey.getApikey());
        if (storedParts.isEmpty()) {
            return false;
        }

        if (!storedParts.get().keyId().equals(apiKeyParts.keyId())) {
            return false;
        }

        return encoder.matches(apiKeyParts.secret().toString(), storedParts.get().hashedSecret());
    }
}
