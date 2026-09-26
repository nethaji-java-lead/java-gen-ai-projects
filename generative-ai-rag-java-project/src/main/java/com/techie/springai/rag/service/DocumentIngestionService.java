package com.techie.springai.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentIngestionService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String uploadFile(MultipartFile file) {

        TikaDocumentReader reader =
                new TikaDocumentReader(file.getResource());

        var documents = reader.read();

        log.info("Documents extracted: {}", documents.size());

        documents.forEach(doc ->
                log.info("Extracted text length: {}", doc.getText().length())
        );

        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(100)
                .withMinChunkSizeChars(20)
                .withMinChunkLengthToEmbed(1)
                .build();

        var chunks = textSplitter.split(documents);

        log.info("Chunks generated: {}", chunks.size());

        vectorStore.accept(chunks);

        return "File uploaded successfully. Chunks: " + chunks.size();
    }
}