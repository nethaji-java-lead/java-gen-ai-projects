package com.example.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GenAIModelService {

    private final ChatClient chatClient;

    public GenAIModelService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Cacheable(cacheNames = "chat-cache")
    public String chat(String question)
    {
        return chatClient
                .prompt()
                .user(question)
                .call()
                .content();
    }
}
