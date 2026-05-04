package org.andromedax.csa.geminiapi.controller;

import jakarta.validation.Valid;
import org.andromedax.csa.geminiapi.model.ChatRequest;
import org.andromedax.csa.geminiapi.model.ChatResponse;
import org.andromedax.csa.geminiapi.service.GeminiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/gemini")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String reply = geminiService.generateReply(request.message().trim());
        return new ChatResponse(reply);
    }
}
