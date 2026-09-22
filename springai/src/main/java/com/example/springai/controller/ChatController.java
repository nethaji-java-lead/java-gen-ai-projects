package com.example.springai.controller;

import com.example.springai.service.GenAIModelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GenAIModelService genAIModelService;

    public ChatController(GenAIModelService genAIModelService) {
        this.genAIModelService = genAIModelService;
    }

    @GetMapping
    public String chat(@RequestParam String question) {

        return genAIModelService.chat(question);
    }
}