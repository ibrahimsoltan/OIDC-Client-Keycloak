package org.valleysoft.app1;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpServletRequest request, Model model) {
        System.out.println("Remote user: " + request.getUserPrincipal().getName());
        request.getAttributeNames().asIterator().forEachRemaining(System.out::println);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication: " + authentication);
        if (authentication != null) {
            System.out.println("Principal: " + authentication.getPrincipal());
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String givenName = oidcUser.getGivenName();
            System.out.println("Given name: " + givenName);
            model.addAttribute("remoteUser", givenName);
        }
        return "index"; // Return the Thymeleaf template named "index"
    }
    @GetMapping("/")
    public String index() {
        return "index"; // Return the same Thymeleaf template for the root URL
    }
}
