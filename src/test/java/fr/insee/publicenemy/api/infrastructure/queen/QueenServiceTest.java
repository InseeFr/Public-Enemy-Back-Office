package fr.insee.publicenemy.api.infrastructure.queen;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.configuration.MetadataProps;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueenServiceTest {
    private static MockWebServer mockWebServer;
    @Mock
    private Questionnaire questionnaire;
    @Mock
    private JsonLunatic jsonLunatic;
    @Mock
    private Ddi ddi;
    @Mock
    private MetadataProps metadataProps;
    private final WebClient webClient = WebClient.create();
    private QueenServiceImpl service;

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
    public void init() {
        String baseUrl = String.format("http://localhost:%s",
                mockWebServer.getPort());
        service = new QueenServiceImpl(webClient, baseUrl, metadataProps);

    }

    @Test
    void onCreateQuestionnaireModelWhenApiResponseErrorThrowsServiceException()  {
        createMockResponseError();
        when(jsonLunatic.jsonContent()).thenReturn("{}");
        assertThrows(ServiceException.class, () -> service.createQuestionnaireModel("l8wwljbo", ddi, jsonLunatic));
    }

    @Test
    void onCreateQuestionnaireModelWhenApiResponseSuccessful() {
        createMockResponseSuccess();
        when(jsonLunatic.jsonContent()).thenReturn("{}");
        assertAll(() -> service.createQuestionnaireModel("l8wwljbo", ddi, jsonLunatic));
    }

    @Test
    void onCreateCampaignWhenApiResponseErrorThrowsServiceException() {
        createMockResponseError();
        assertThrows(ServiceException.class, () -> service.createCampaign("l8wwljbo-CAPI", questionnaire, ddi));
    }

    @Test
    void onCreateCampaignWhenApiResponseSuccessful() {
        createMockResponseSuccess();
        assertAll(() -> service.createCampaign("l8wwljbo-CAPI", questionnaire, ddi));
    }

    @Test
    void onDeleteCampaignWhenApiResponseErrorThrowsServiceException() {
        createMockResponseError();
        assertThrows(ServiceException.class, () -> service.deleteCampaign("l8wwljbo-CAPI"));
    }

    @Test
    void onDeleteCampaignWhenApiResponseSuccessful() {
        createMockResponseSuccess();
        assertAll(() -> service.deleteCampaign("l8wwljbo-CAPI"));
    }

    /**
     * Create api empty response whit status 200
     */
    private void createMockResponseSuccess() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
    }

    /**
     * Create api response error
     */
    private void createMockResponseError() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(500)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{}")
        );
    }
}