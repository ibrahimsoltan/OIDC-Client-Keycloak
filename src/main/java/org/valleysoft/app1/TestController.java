package org.valleysoft.app1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Controller
public class TestController {
    @GetMapping("/test")
    public String test(Principal principal) {
        return "test";
    }

    @GetMapping("/test2")
    public String test2() {
        return "test2";
    }


}