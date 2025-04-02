package fr.insee.publicenemy.api.infrastructure.pogues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.infrastructure.pogues.exceptions.LunaticJsonNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnoServiceTest {

    private EnoServiceImpl service;

    private static MockWebServer mockWebServer;

    private final WebClient webClient = WebClient.create();

    @Mock
    private QuestionnaireModel questionnaireModel;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void init() throws JsonProcessingException {
        String enoUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        service = new EnoServiceImpl(webClient, enoUrl);
        when(questionnaireModel.content()).thenReturn(new ObjectMapper().readTree("{}"));
    }

    @Test
    void onGetJsonLunaticReturnsJsonLunatic() throws IOException {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;
        String jsonContent = "{\"id\": \"l8wwljbo\"}";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonContent)
        );

        JsonLunatic jsonLunatic = service.getJsonLunatic(questionnaireModel, context, mode);

        assertEquals(jsonLunatic, new JsonLunatic(jsonContent));
    }

    @Test
    void onGetJsonLunaticWhenEnoEmptyResponseThrowsJsonLunaticNotFoundException() {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;

        // empty response
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        assertThrows(LunaticJsonNotFoundException.class, () -> service.getJsonLunatic(questionnaireModel, context, mode));
    }

    @Test
    void onGetJsonLunaticWhenEnoNotFoundResponseThrowsJsonLunaticNotFoundException() {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;

        // 404 status
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        assertThrows(LunaticJsonNotFoundException.class, () -> service.getJsonLunatic(questionnaireModel, context, mode));
    }

    @Test
    void onGetQuestionnaireWhenEnoErrorThrowsServiceException() {
        Context context = Context.BUSINESS;
        Mode mode = Mode.CAWI;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{}")
        );

        assertThrows(ServiceException.class, () -> service.getJsonLunatic(questionnaireModel, context, mode));
    }
}
