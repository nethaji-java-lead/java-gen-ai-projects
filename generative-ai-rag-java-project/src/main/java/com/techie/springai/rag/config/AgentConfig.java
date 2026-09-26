package com.techie.springai.rag.config;

import com.techie.springai.rag.service.AssistantAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public AssistantAgent getAssistance(ChatModel chatModel) {
        return AiServices.builder(AssistantAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("http://localhost:1234/v1")
                .apiKey("lm-studio")
                .modelName("qwen/qwen3-vl-4b")
                .build();
    }

}
