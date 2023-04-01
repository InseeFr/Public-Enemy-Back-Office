package fr.insee.publicenemy.api.infrastructure.ddi;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.DdiNotFoundException;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.PoguesJsonNotFoundException;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DdiPoguesServiceTest {

    private DdiPoguesServiceImpl service;

    private static MockWebServer mockWebServer;

    private final WebClient webClient = WebClient.create();

    private final String poguesId = "l8wwljbo";
    private final List<Mode> modes = List.of(Mode.CAWI, Mode.CAPI);

    private final String ddiContent = "<xml></xml>";

    @Mock
    private I18nMessagePort messageService;

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
        String poguesUrl = String.format("http://localhost:%s",
                mockWebServer.getPort());
        service = new DdiPoguesServiceImpl(webClient, poguesUrl, messageService);


    }

    @Test
    void onGetQuestionnaireFromPoguesReturnCorrespondingQuestionnaire() {
        // call to get json pogues
        createMockPoguesResponseSuccess(modes);

        Questionnaire questionnaire = service.getQuestionnaire(poguesId);

        assertEquals(questionnaire, new Questionnaire(poguesId, "questionnaire label", modes));
    }

    @Test
    void onGetQuestionnaireWhenNotFoundResponseThrowsPoguesJsonNotFoundException() {
        // call to get json pogues that return not found status
        createMockNotFoundResponse();

        assertThrows(PoguesJsonNotFoundException.class, () -> service.getQuestionnaire(poguesId));
    }

    @Test
    void onGetQuestionnaireWhenEmptyResponseThrowsPoguesJsonNotFoundException() {
        // call to get json pogues that return not found status
        createMockEmptyResponse();

        assertThrows(PoguesJsonNotFoundException.class, () -> service.getQuestionnaire(poguesId));
    }

    @Test
    void onGetQuestionnaireWhenErrorFromPoguesThrowsServiceException() {
        // call to get json pogues that return error status
        createMockResponseError();

        assertThrows(ServiceException.class, () -> service.getQuestionnaire(poguesId));
    }

    @Test
    void onGetDdiReturnCorrectDdi() {
        // call to get json pogues that returns ok status
        createMockPoguesResponseSuccess(modes);

        // call to get xml ddi from eno that return ok status
        createMockEnoResponseSuccess();

        Ddi ddi = service.getDdi(poguesId);

        String label = "questionnaire label";
        assertEquals(ddi, new Ddi(poguesId, label, modes, ddiContent.getBytes()));
    }

    @Test
    void onGetDdiWhenDdiWhenEmptyResponseThrowsDdiNotFoundException() {
        // call to get json pogues that returns ok status
        createMockPoguesResponseSuccess(modes);

        // call to get xml ddi from eno that returns ok status with empty body
        createMockEmptyResponse();

        assertThrows(DdiNotFoundException.class, () -> service.getDdi(poguesId));
    }

    @Test
    void onGetDdiWhenDdiNotFoundThrowsDdiNotFoundException() {
        // call to get json pogues that returns ok status
        createMockPoguesResponseSuccess(modes);

        // call to get xml ddi from eno that returns not found status
        createMockNotFoundResponse();

        assertThrows(DdiNotFoundException.class, () -> service.getDdi(poguesId));
    }

    @Test
    void onGetDdiWhenEnoErrorThrowsServiceException() {
        // call to get json pogues that returns ok status
        createMockPoguesResponseSuccess(modes);

        // call to get xml ddi from eno that returns error status
        createMockResponseError();

        assertThrows(ServiceException.class, () -> service.getDdi(poguesId));
    }

    @Test
    void onGetDdiWhenPoguesIdNullThrowsException() {
        assertThrows(NullPointerException.class, () -> service.getDdi(null));
    }

    @Test
    void onGetQuestionnaireWhenPoguesIdNullThrowsException() {
        assertThrows(NullPointerException.class, () -> service.getQuestionnaire(null));
    }

    @Test
    void onGetQuestionnaireVariablesWhenPoguesIdNullThrowsException() {
        assertThrows(NullPointerException.class, () -> service.getQuestionnaireVariables(null));
    }

    @Test
    void onGetQuestionnaireVariablesWhenQuestionnaireNotFoundThrowsError() {
        createMockNotFoundResponse();
        assertThrows(PoguesJsonNotFoundException.class, () -> service.getQuestionnaireVariables(poguesId));
    }

    @Test
    void onGetQuestionnaireVariablesWhenErrorResponseThrowsError() {
        createMockResponseError();
        assertThrows(ServiceException.class, () -> service.getQuestionnaireVariables(poguesId));
    }

    @Test
    void onGetQuestionnaireVariablesWhenContentResponseIncorrectThrowsError() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("{ incorrect content }")
        );
        assertThrows(ServiceException.class, () -> service.getQuestionnaireVariables(poguesId));
    }

    @Test
    void onGetQuestionnaireVariablesReturnVariables() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("""
                                [
                                  {
                                    "type": "ExternalVariableType",
                                    "Datatype": {
                                      "type": "TextDatatypeType",
                                      "typeName": "TEXT",
                                      "MaxLength": 249,
                                      "Pattern": ""
                                    },
                                    "Name": "ADMINISTRATION1",
                                    "Scope": "l8oatijq"\s
                                  },
                                  {
                                    "type": "CalculatedVariableType",
                                    "Datatype": {
                                      "type": "TextDatatypeType",
                                      "typeName": "TEXT",
                                      "MaxLength": 249,
                                      "Pattern": ""
                                    },
                                    "Name": "ADMINISTRATION2",
                                    "Scope": null
                                  }]""")
        );

        List<VariableType> variables = service.getQuestionnaireVariables(poguesId);
        assertEquals(2, variables.size());
        assertEquals("ADMINISTRATION1", variables.get(0).name());
        assertEquals("l8oatijq", variables.get(0).scope());
        assertEquals(VariableTypeEnum.EXTERNAL, variables.get(0).type());
        assertEquals("ADMINISTRATION2", variables.get(1).name());
        assertNull(variables.get(1).scope());
        assertEquals(VariableTypeEnum.CALCULATED, variables.get(1).type());
    }

    /**
     * Create pogues api empty response with status 200
     */
    private void createMockPoguesResponseSuccess(List<Mode> modes) {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(
                                String.format("{\"TargetMode\": [\"%s\",\"%s\"], \"Label\": [\"%s\"]}", modes.get(0).name(), modes.get(1).name(), "questionnaire label")
                        )
        );
    }

    /**
     * Create pogues api empty response with status 200
     */
    private void createMockEnoResponseSuccess() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                        .setBody(ddiContent)
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
     * Create api empty response
     */
    private void createMockEmptyResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
    }

    /**
     * Create api response not found
     */
    private void createMockNotFoundResponse() {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(404)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );
    }
}
