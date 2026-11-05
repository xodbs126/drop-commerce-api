package com.xodbs126.dropcommerce.global.interceptor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginCheckTestController {

    @GetMapping("/test/auth")
    public ResponseEntity<Void> auth() {
        return ResponseEntity.noContent().build();
    }
}
