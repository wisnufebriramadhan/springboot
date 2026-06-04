package com.startechinnovation.userapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello Brok"
        );
    }

    @GetMapping("/hello/{name}")
    public Map<String, String> hello(
            @PathVariable String name
    ) {
        return Map.of(
                "message", "Hello " + name
        );
    }
}
