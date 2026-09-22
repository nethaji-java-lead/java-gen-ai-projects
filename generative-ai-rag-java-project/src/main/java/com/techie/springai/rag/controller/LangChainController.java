package com.techie.springai.rag.controller;


import com.techie.springai.rag.domain.ChatRequest;
import com.techie.springai.rag.domain.ChatResponse;
import com.techie.springai.rag.service.AssistantAgent;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/langchain")
public class LangChainController {

    private final AssistantAgent assistantAgent;

    private final ChatMemoryProvider chatMemoryProvider;

    public LangChainController(AssistantAgent assistantAgent, ChatMemoryProvider chatMemoryProvider) {
        this.assistantAgent = assistantAgent;
        this.chatMemoryProvider = chatMemoryProvider;
    }


    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatWithLangChain(@RequestBody ChatRequest chatRequest) {
        String response = assistantAgent.chat(chatRequest.userId(), chatRequest.question());

        // Retrieve the memory instance for this userId directly from the provider
        ChatMemory chatMemory = chatMemoryProvider.get(chatRequest.userId());
        if (Objects.nonNull(chatMemory)) {
            chatMemory.messages().forEach(message ->
                    System.out.println("Message: " + message));
        }

        return new ResponseEntity<>(new ChatResponse(response), HttpStatus.OK);
    }
}
