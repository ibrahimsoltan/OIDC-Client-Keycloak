package org.valleysoft.app1;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        // Store ID token in the session
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        System.out.println("ID token from load user: " + oidcUser.getIdToken().getTokenValue());
        System.out.println("session: " + request.getSession().getId());
        request.getSession().setAttribute("id_token", oidcUser.getIdToken().getTokenValue());

        // Set the authentication in the SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                oidcUser, null, oidcUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Return the oidcUser to be saved in the authentication object
        return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
