package org.andromedax.csa.geminiapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
@TestPropertySource(properties = "app.gemini.model=gemini-3.1-flash-lite")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsOkStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "ok",
                          "service": "gemini-api",
                          "model": "gemini-3.1-flash-lite"
                        }
                        """));
    }
}
