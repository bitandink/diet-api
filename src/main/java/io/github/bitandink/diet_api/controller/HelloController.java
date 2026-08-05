package io.github.bitandink.diet_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Diet API 서버가 정상적으로 실행 중입니다.";
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}