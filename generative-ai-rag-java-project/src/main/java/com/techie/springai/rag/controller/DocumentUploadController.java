package com.techie.springai.rag.controller;

import com.techie.springai.rag.domain.ChatRequest;
import com.techie.springai.rag.domain.ChatResponse;
import com.techie.springai.rag.service.AssistantAgent;
import com.techie.springai.rag.service.DocumentIngestionService;
import com.techie.springai.rag.service.RagService;
import dev.langchain4j.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping("/api/doc")
public class DocumentUploadController {

    private final AssistantAgent assistantAgent;

    private final RagService ragService;

    private final DocumentIngestionService documentIngestionService;

    public DocumentUploadController(AssistantAgent assistantAgent, RagService ragService, DocumentIngestionService documentIngestionService) {
        this.assistantAgent = assistantAgent;
        this.ragService = ragService;
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file)
    {
        String response = documentIngestionService.uploadFile(file);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/chat")
    @Scheduled(**00:01)
    public ResponseEntity<ChatResponse> chatWithLangChain(@RequestBody String question) {
        String response = ragService.ask(question);

        return new ResponseEntity<>(new ChatResponse(response), HttpStatus.OK);
    }
}
