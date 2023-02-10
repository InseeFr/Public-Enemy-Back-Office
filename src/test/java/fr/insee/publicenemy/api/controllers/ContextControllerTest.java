package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContextController.class)
@ContextConfiguration(classes = ContextController.class)
class ContextControllerTest {
    @MockBean
    private I18nMessagePort i18nService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        when(i18nService.getMessage(any(), any())).thenReturn("value");
    }

    @Test
    void onGetContextsReturnsAllContexts() throws Exception {
        List<String> contextNames = Arrays.stream(Context.values()).map(Enum::name).toList();
        mockMvc.perform(get("/api/contexts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].name", is(contextNames.get(0))))
                .andExpect(jsonPath("$[1].name", is(contextNames.get(1))));
    }
}

