package com.techie.springai.rag.service;


import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class GreetingTool {

    @Tool("Greet the user with a personalized message when they say 'Hi'. Use this for any query related to greetings.'")
    public String getGreeting(String name) {
        return String.format("Hello, %s! How can I assist you today?", name);
    }
}
