package com.integrixs.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebSocketTestPageController {

    @GetMapping("/ws-test")
    public String websocketTest() {
        return "websocket-test.html";
    }
}