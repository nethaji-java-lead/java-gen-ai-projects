package com.techie.springai.rag.service;


import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProductTool {

    private Map<String, String> productMap = Map.of(
            "iPhone", "iPhone is a smartphone developed by Apple.",
            "iPad", "iPad is a tablet computer developed by Apple.",
            "MacBook", "MacBook is a line of laptop computers developed by Apple."
    );

    @Tool("Lookup product information by name, Use this for any query related to product information.If the product is not found, respond with 'Product not found.'")
    public String getProductInformation(String productName) {
        return productMap.getOrDefault(productName, "Product not found.");
    }
}
