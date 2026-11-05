package com.moneyai.controller;

import com.moneyai.dto.ChatRequest;
import com.moneyai.dto.ChatResponse;
import com.moneyai.service.AdvisorService;
import com.moneyai.service.DocumentReaderService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AdvisorService advisor;
    private final DocumentReaderService reader;

    public ChatController(AdvisorService advisor, DocumentReaderService reader) {
        this.advisor = advisor;
        this.reader = reader;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return advisor.reply(request.message());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        String content = reader.read(file);
        return ChatResponse.of(advisor.analyzeDocument(file.getOriginalFilename(), content), "Ver meu saldo");
    }
}
