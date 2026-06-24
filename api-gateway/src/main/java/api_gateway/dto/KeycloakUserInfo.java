package api_gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class KeycloakUserInfo {

    private String sub;
    private String name;
    private String email;

    @JsonProperty("realm_access")
    private Map<String, List<String>> realmAccess;

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, List<String>> getRealmAccess() { return realmAccess; }
    public void setRealmAccess(Map<String, List<String>> realmAccess) { this.realmAccess = realmAccess; }

    public List<String> getRoles() {
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return realmAccess.get("roles");
        }
        return List.of();
    }
}
