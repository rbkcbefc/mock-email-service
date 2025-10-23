package com.cicdaas.mockemailservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MvcController.class)
class MvcControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHomeReturnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

    @Test
    void testHomeMappedToRootPath() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());
    }
}
