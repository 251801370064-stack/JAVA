package com.library.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WebApplicationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void servesTheBrowserApplication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));
        }

        @Test
        void servesTheBrowserJavaScript() throws Exception {
        mockMvc.perform(get("/app.js"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("initialize")));
    }

    @Test
    void protectsLibraryApiWithoutAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please sign in")));
    }
}
