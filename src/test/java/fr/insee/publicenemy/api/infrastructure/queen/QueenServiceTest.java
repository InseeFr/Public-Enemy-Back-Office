package fr.insee.publicenemy.api.infrastructure.queen;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.configuration.MetadataProps;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import fr.insee.publicenemy.api.infrastructure.queen.exceptions.SurveyUnitsNotFoundException;
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
import java.util.ArrayList;
import java.util.List;

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

        QuestionnaireMode questionnaireMode = new QuestionnaireMode(Mode.CAWI);
        List<QuestionnaireMode> questionnaireModes = List.of(questionnaireMode);
        questionnaire = new Questionnaire(1L, "l8wwljbo", "label", Context.BUSINESS,
                questionnaireModes, "data".getBytes(),  false);
    }

    @Test
    void onCreateSurveyUnitsWhenApiResponseSuccessfulReturnNothing() {
        SurveyUnitData data = new SurveyUnitData(new ArrayList<>());
        List<SurveyUnit> surveyUnits = new ArrayList<>();
        // Create success response for each survey unit creation
        for(long nbSurveyUnits=1; nbSurveyUnits<=4; nbSurveyUnits++) {
            surveyUnits.add(new SurveyUnit("id"+nbSurveyUnits, "q"+nbSurveyUnits, data, SurveyUnitStateData.createInitialStateData()));
            createMockResponseSuccess();
        }
        assertAll(() -> service.createSurveyUnits("12-CAWI", surveyUnits));
    }

    @Test
    void onCreateQuestionnaireModelWhenApiResponseErrorThrowsServiceException()  {
        createMockResponseError();
        when(jsonLunatic.jsonContent()).thenReturn("{}");
        assertThrows(ServiceException.class, () -> service.createQuestionnaireModel("l8wwljbo", ddi, jsonLunatic));
    }

    @Test
    void onCreateQuestionnaireModelWhenApiResponseSuccessfulReturnNothing() {
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
    void onCreateSurveyUnitsWhenApiResponseErrorThrowsServiceException() {
        createMockResponseError();
        SurveyUnitData data = new SurveyUnitData(new ArrayList<>());
        List<SurveyUnit> surveyUnits = new ArrayList<>();
        surveyUnits.add(new SurveyUnit("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("2", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnit("3", "q1", data, SurveyUnitStateData.createInitialStateData()));
        assertThrows(ServiceException.class, () -> service.createSurveyUnits("12-CAWI", surveyUnits));
    }

    @Test
    void onGetSurveyUnitsWhenEmptyResponseThrowsSurveyUnitsNotFoundException() {
        createMockEmptyResponse();
        assertThrows(SurveyUnitsNotFoundException.class, () -> service.getSurveyUnits("1"));
    }

    @Test
    void onGetSurveyUnitsWhenNotFoundResponseThrowsSurveyUnitsNotFoundException() {
        createMockNotFoundResponse();
        assertThrows(SurveyUnitsNotFoundException.class, () -> service.getSurveyUnits("1"));
    }

    @Test
    void onCreateCampaignWhenApiResponseSuccessfulReturnNothing() {
        createMockResponseSuccess();
        assertAll(() -> service.createCampaign("l8wwljbo-CAPI", questionnaire, ddi));
    }

    @Test
    void onDeleteCampaignWhenApiResponseErrorThrowsServiceException() {
        createMockResponseError();
        assertThrows(ServiceException.class, () -> service.deleteCampaign("l8wwljbo-CAPI"));
    }

    @Test
    void onDeleteCampaignWhenApiResponseSuccessfulReturnNothing() {
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
                        .setBody("{}")
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

    /**
     * Create api response empty
     */
    private void createMockEmptyResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
    }

    /**
     * Create api response empty
     */
    private void createMockNotFoundResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
    }
}