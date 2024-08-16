# Keycloak Integration with Spring Boot Application

## Overview

This repository demonstrates how to integrate Keycloak, an open-source Identity and Access Management system, with a Spring Boot application. The integration allows secure login using OAuth2/OpenID Connect protocols. This setup is designed to allow authentication through Keycloak and implements user-specific role-based access control (RBAC) within the application.

## Key Components

### 1. `SecurityConfig.java`
This class contains the security configuration for the Spring Boot application.

- **HttpSecurity Configuration**: Configures the application's security, including login, logout, and access control for various endpoints.
- **OAuth2 Login**: Enables OAuth2 login using Keycloak, specifying the authorization endpoint, token endpoint, and user info endpoint.
- **Resource Server**: Configures the application as a resource server to validate JWT tokens issued by Keycloak.
- **Logout Configuration**: Handles Keycloak-based logout and session management, ensuring proper cleanup after user logout.

### 2. Keycloak Client Registration
The Keycloak client registration defines the connection details between the Spring Boot application and Keycloak, including client ID, client secret, authorization URI, and token URI.

### 3. `CustomOidcUserService.java`
This service handles the custom mapping of user details fetched from the Keycloak server.

## Prerequisites

1. **Java 8 or higher**: The project is built using Java.
2. **Spring Boot**: Ensure that Spring Boot dependencies are correctly included in your `pom.xml` file.
3. **Keycloak Server**: You need a running Keycloak server with a realm and client configured.

## Key Configuration Details

### 1. Security Configuration

- **`SecurityFilterChain`**: The security filter chain configures the security settings for the application, including HTTP security and OAuth2 login. The filter chain also defines the default success URL and handles user info fetching.

- **`ClientRegistrationRepository`**: Defines how the client (your Spring Boot app) registers with the Keycloak server. This includes the client ID, secret, authorization grant type, and the endpoints for authorization, tokens, and user information.

### 2. Keycloak Logout Handling

- **OIDC Logout**: Configures the logout success handler to ensure users are logged out of both the application and Keycloak.

## Running the Application

1. **Start Keycloak Server**: Ensure your Keycloak server is running and configured with the appropriate client and realm.
2. **Run Spring Boot Application**: Start the Spring Boot application using your preferred method (e.g., `mvn spring-boot:run`).
3. **Access the Application**: Navigate to `http://localhost:8081/home` to trigger the login process via Keycloak.

## Keycloak Configuration Steps

1. **Create a Realm**: Create a realm in your Keycloak instance.
2. **Create a Client**: Configure a client in the Keycloak realm. This client will represent your Spring Boot application.
3. **Define Roles**: Set up roles in Keycloak and assign them to users.
4. **Mapping Roles to Spring Boot**: Ensure roles are mapped correctly within the Spring Boot application for role-based access control.

## Adding the Keycloak Plugin (Optional)

If you want to add a Keycloak plugin for extended functionality, like restricting client authentication, you can do so using Docker. Here's an example command to run Keycloak with a custom plugin:

```bash
docker run -p 8080:8080 --name keycloak-with-plugin-container \
-v /path/to/plugin.jar:/opt/keycloak/providers/plugin.jar \
quay.io/keycloak/keycloak:latest start-dev
```


## Troubleshooting

- **403 Forbidden**: If you encounter a 403 error, ensure that the user's roles in Keycloak are correctly mapped to the application's roles.
- **Connection Refused**: Ensure that the Keycloak server is running and accessible from the Spring Boot application.

## License

This project is licensed under the MIT License.

## Contribution

Feel free to submit pull requests or open issues if you encounter any problems or have suggestions for improvements.
