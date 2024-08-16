package org.valleysoft.app1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomOidcUserService customOidcUserService;

    // Define the SecurityFilterChain bean to configure HTTP security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/test").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .defaultSuccessUrl("/home", true)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    // Define the ClientRegistrationRepository bean to configure OAuth2 client registrations
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.keycloakClientRegistration());
    }

    // Define the Keycloak client registration
    private ClientRegistration keycloakClientRegistration() {
        return ClientRegistration.withRegistrationId("keycloak")
                .clientId("app1") // Client ID registered in Keycloak
                .clientSecret("UJK71YF1DluubHcTndZvsc4hCUrIFLvw") // Client secret from Keycloak
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // Use authorization code grant type
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}") // Redirect URI for OAuth2 login
                .scope("openid", "profile", "email") // Scopes requested
                .authorizationUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/auth") // Keycloak authorization endpoint
                .tokenUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/token") // Keycloak token endpoint
                .userInfoUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/userinfo") // Keycloak user info endpoint
                .userNameAttributeName(IdTokenClaimNames.SUB) // Claim name for the user's identifier
                .jwkSetUri("http://localhost:8080/realms/MyRealm/protocol/openid-connect/certs") // Keycloak JWK set endpoint
                .clientName("Keycloak") // Client name
                .build();
    }
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository());

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://localhost:8081/test");

        return (request, response, authentication) -> {
            System.out.println("Authentication from logout: " + authentication);
            if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
                String idToken = oidcUser.getIdToken().getTokenValue();
                System.out.println("ID Token during logout: " + idToken);

                String logoutUrl = "http://localhost:8080/realms/MyRealm/protocol/openid-connect/logout?id_token_hint=" + idToken + "&post_logout_redirect_uri=" + "http://localhost:8081/test";
                System.out.println("Logout URL: " + logoutUrl);

                response.sendRedirect(logoutUrl);
            } else {
                System.out.println("Authentication is null or not an instance of OidcUser");
                response.sendRedirect("/login");
            }
        };
    }
}
