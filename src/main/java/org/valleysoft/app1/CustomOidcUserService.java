package org.valleysoft.app1;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // Load the user using the default implementation
        OidcUser oidcUser = super.loadUser(userRequest);

        // Extract the "groups" claim from the ID token
        Map<String, Object> claims = oidcUser.getClaims();
        List<GrantedAuthority> groupAuthorities = claims.containsKey("groups") ?
                ((List<String>) claims.get("groups")).stream()
                        .map(group -> group.startsWith("/") ? group.substring(1) : group) // Remove leading '/'
                        .map(group -> "ROLE_" + group) // Add ROLE_ prefix
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()) : List.of();

        // Create a new modifiable collection of authorities
        Collection<GrantedAuthority> mappedAuthorities = new ArrayList<>(oidcUser.getAuthorities());
        mappedAuthorities.addAll(groupAuthorities);

        // Log the claims and authorities for debugging
        System.out.println("OIDC Claims: " + claims);
        System.out.println("Mapped Authorities: " + mappedAuthorities);

        // Store ID token in the session
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        System.out.println("ID token from load user: " + oidcUser.getIdToken().getTokenValue());
        System.out.println("Session: " + request.getSession().getId());
        request.getSession().setAttribute("id_token", oidcUser.getIdToken().getTokenValue());

        // Set the authentication in the SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                oidcUser, null, mappedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Return a new OIDC user with the updated authorities
        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
