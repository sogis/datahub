package ch.so.agi.datahub;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

public class CustomRealm extends RealmBase {
    
    private final Map<String, String> users = new HashMap<>(); 
    private final Map<String, String> roles = new HashMap<>(); 

    public CustomRealm(String username, String password, String role) {
        users.put(username, password);  
        roles.put(username, role);     
    }

    @Override
    protected String getPassword(String username) {
        return users.get(username);
    }

    @Override
    protected Principal getPrincipal(String username) {
        return new GenericPrincipal(username, getPassword(username), List.of(roles.get(username)));
    }
}
