package com.techie.springai.rag.service;


import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @Tool("Get the current weather information for a given city. Use this for any query related to weather information. If the city is not found, respond with 'City not found.'")
    public String getWeatherInformation(String city) {
        return String.format("The current weather in %s is sunny with a temperature of 25°C.", city);
    }
}
