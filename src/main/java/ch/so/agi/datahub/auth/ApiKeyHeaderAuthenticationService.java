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
        if (apiKeyParts.isPresent()) {
            Optional<CoreApikey> apiKey = loadActiveApiKeyById(apiKeyParts.get().keyId());
            if (apiKey.isPresent() && matchesStoredKey(apiKeyParts.get(), apiKey.get())) {
                return authenticate(apiKeyAuthenticationToken, apiKey.get());
            }
            return apiKeyAuthenticationToken;
        }

        List<CoreApikey> apiKeys = loadActiveApiKeys();

        // Weil das Passwort randommässig gesalted wird, muss man die matches-Funktion verwenden und
        // kann nicht den Plaintext-Key nochmals encoden und mit der DB mittels SQL vergleichen.
        for (CoreApikey apiKey : apiKeys) {
            if (ApiKeyFormat.parseStoredValue(apiKey.getApikey()).isPresent()) {
                continue;
            }
            if (encoder.matches(apiKeyAuthenticationToken.getApiKey(), apiKey.getApikey())) {
                return authenticate(apiKeyAuthenticationToken, apiKey);
            }
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

    protected List<CoreApikey> loadActiveApiKeys() {
        return ObjectSelect.query(CoreApikey.class)
                .where(CoreApikey.REVOKEDAT.isNull())
                .and(CoreApikey.DATEOFEXPIRY.gt(LocalDateTime.now()).orExp(CoreApikey.DATEOFEXPIRY.isNull()))
                .select(objectContext);
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
