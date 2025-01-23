package org.valleysoft.app1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class KeycloakUserController {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    // Serve the Thymeleaf page
    @GetMapping("/create-user")
    @PreAuthorize("hasRole('admin')") // Restrict access to users in the admin group
    public String showCreateUserForm(Model model) {
        model.addAttribute("message", null); // Placeholder for success/error messages
        List<String> groups = keycloakAdminService.getGroups();
        System.out.println("Groups: " + groups);
        model.addAttribute("groups", groups);
        return "admin";
    }

    // Handle form submission
    @PostMapping("/create-user")
    @PreAuthorize("hasRole('admin')") // Restrict access to users in the admin group
    public String createUser(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam List<String> groups,
            @RequestParam String password,
            Model model) {
        try {
            boolean created = keycloakAdminService.createUser(username, email,  password, groups);
            if (created) {
                model.addAttribute("message", "User created successfully!");
            } else {
                model.addAttribute("message", "Failed to create user.");
            }
        } catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "admin"; // Redirect back to the same page with a message
    }
}
