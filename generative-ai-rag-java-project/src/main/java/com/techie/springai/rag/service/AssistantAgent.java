package com.techie.springai.rag.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AssistantAgent {

    @SystemMessage("""
             You are a helpful assistant that answers questions based on the provided context.
             - If the context does not contain the answer, respond with I don't know.
             - Get the weather for any city.
             - Perform calculations.
             - Lookup for product information.
             - Always be concise and to the point.""")
    String chat(@UserMessage String question);
}
