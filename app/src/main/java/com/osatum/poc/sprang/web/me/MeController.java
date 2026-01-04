package com.osatum.poc.sprang.web.me;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Me")
@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public String me(Authentication auth) {
        return auth.getName();
    }

    @GetMapping("/admin/ping")
    public String adminPing() {
        return "pong";
    }
}
