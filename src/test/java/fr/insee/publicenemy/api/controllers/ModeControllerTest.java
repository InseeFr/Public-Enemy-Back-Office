package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeController.class)
@ContextConfiguration(classes = ModeController.class)
class ModeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onGetModesReturnsAllModes() throws Exception {
        Mode[] modes = Mode.values();
        mockMvc.perform(get("/api/modes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(modes.length)));
    }
}
