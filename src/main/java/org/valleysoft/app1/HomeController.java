package org.valleysoft.app1;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("remoteUser", request.getRemoteUser());
        return "index"; // Return the Thymeleaf template named "index"
    }
    @GetMapping("/")
    public String index() {
        return "index"; // Return the same Thymeleaf template for the root URL
    }
}
