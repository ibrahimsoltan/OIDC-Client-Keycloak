package org.valleysoft.app1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private RestTemplate restTemplate = new RestTemplate();

    public boolean createUser(String username, String email,String password, List<String> groups) {
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("Failed to fetch admin access token from Keycloak");
        }

//        // Check if the email domain is valid
//        if (!email.endsWith("@example.com")) { // Replace with your domain(s)
//            throw new RuntimeException("Invalid email domain. Allowed domains are: @example.com");
//        }

        // Prepare the user payload
        Map<String, Object> user = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "credentials", Collections.singletonList(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", true
                ))
        );

        // Send the user creation request
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);


        if (response.getStatusCode() == HttpStatus.CREATED) {
            // User created successfully, fetch the user ID
            String userId = getUserIdByUsername(username, accessToken);
            if (userId == null) {
                throw new RuntimeException("User created but could not fetch user ID.");
            }

            // Assign the user to the specified groups
            for (String group : groups) {
                assignGroupToUser(userId, group, accessToken);
            }

            return true;
        }

        return false;
    }


    public List<String> getGroups() {
        String accessToken = getAdminAccessToken();

        String url = keycloakUrl + "/admin/realms/" + realm + "/groups";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        System.out.println("response of getGroups: " + response);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            // Extract the group names
            return (List<String>) response.getBody().stream()
                    .map(group -> ((Map<String, Object>) group).get("name").toString())
                    .collect(Collectors.toList());
        }
        throw new RuntimeException("Failed to fetch groups from Keycloak");
    }

    public boolean addUserToGroups(String username, List<String> groups) {
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("Failed to fetch admin access token from Keycloak");
        }

        // Fetch the user ID by username
        String userId = getUserIdByUsername(username, accessToken);
        if (userId == null) {
            throw new RuntimeException("User with username " + username + " not found in Keycloak.");
        }

        // Assign groups to the user
        for (String group : groups) {
            assignGroupToUser(userId, group, accessToken);
        }

        return true;
    }

    private String getUserIdByUsername(String username, String accessToken) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
            return (String) user.get("id");
        }

        return null;
    }

    private void assignGroupToUser(String userId, String groupName, String accessToken) {
        // Fetch group ID by group name
        String groupId = getGroupIdByName(groupName, accessToken);
        if (groupId == null) {
            throw new RuntimeException("Group with name " + groupName + " not found in Keycloak.");
        }

        // Assign the group to the user
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/groups/" + groupId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to assign group " + groupName + " to user " + userId);
        }
    }

    private String getGroupIdByName(String groupName, String accessToken) {
        String url = keycloakUrl + "/admin/realms/" + realm + "/groups";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            for (Object groupObj : response.getBody()) {
                Map<String, Object> group = (Map<String, Object>) groupObj;
                if (groupName.equals(group.get("name"))) {
                    return (String) group.get("id");
                }
            }
        }

        return null;
    }




    private String getAdminAccessToken() {
        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&grant_type=client_credentials";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return (String) response.getBody().get("access_token");
        }
        return null;
    }
}
