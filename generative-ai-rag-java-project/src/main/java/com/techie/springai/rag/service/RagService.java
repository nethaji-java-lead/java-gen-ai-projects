package com.techie.springai.rag.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    public RagService(VectorStore vectorStore, ChatModel chatModel) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
    }

    public String ask(String question) {

        // 1. Similarity search in PGVector
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .build()
        );

        System.out.println("documents: "+documents);

        if (documents.isEmpty()) {
            return "I could not find relevant information in the uploaded documents.";
        }

        // 2. Build context from retrieved documents
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. Prompt Qwen with retrieved context
        String prompt = """
                You are a helpful RAG assistant.

                Answer the user's question using ONLY the information
                provided in the context below.

                If the answer cannot be found in the context,
                say that the information is not available in the documents.

                Context:
                %s

                User Question:
                %s

                Answer:
                """.formatted(context, question);

        return chatModel.chat(prompt);
    }
}
