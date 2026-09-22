package com.techie.springai.rag.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AgentConfig {

    @Value("${chat.api.key}")
    private String apiKey;

    @Value("${chat.model.name}")
    private String chatModelName;

    private final Map<Object, ChatMemory> chatMemoryStore = new ConcurrentHashMap<>();

    /*@Bean
    public ChatModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModelName)
                .sendThinking(true)   // Ensures thinking metadata is preserved
                .returnThinking(true) // Round-trips thought_signature during tool calls
                .build();
    }*/

    @Bean
    public ChatModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl("http://localhost:1234/v1")
                .apiKey("lm-studio")
                .modelName("qwen3-vl-4b")
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> chatMemoryStore.computeIfAbsent(memoryId, id ->
                MessageWindowChatMemory.withMaxMessages(10));
    }

    @Bean
    public Map<Object, ChatMemory> chatMemoryStore() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Cache<Long, ChatMemory> chatMemoryCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .build();
    }
}
