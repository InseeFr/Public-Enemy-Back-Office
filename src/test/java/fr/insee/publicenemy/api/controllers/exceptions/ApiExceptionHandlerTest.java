package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.infrastructure.i18n.I18nMessageServiceImpl;
import fr.insee.publicenemy.api.infrastructure.pogues.exceptions.LunaticJsonNotFoundException;
import fr.insee.publicenemy.api.infrastructure.pogues.exceptions.PoguesJsonNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {
    private ApiExceptionHandler handler;
    @Mock
    private ApiExceptionComponent errorComponent;

    @Mock
    private I18nMessageServiceImpl messageService;

    @Mock
    private ServletWebRequest request;

    @BeforeEach
    public void init() {
        handler = new ApiExceptionHandler(errorComponent, messageService);
    }

    @Test
    void onHandleServiceExceptionReturnCorrectApiError() {
        ServiceException exception = new ServiceException(HttpStatus.NOT_FOUND, "message");
        ApiError apiError = new ApiError(exception.getStatus().value(), "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(apiError);
        ResponseEntity<ApiError> response = handler.handleServiceException(exception, request);
        assertEquals(exception.getStatus().value(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandlePoguesJsonNotFoundExceptionReturnCorrectApiError() {
        PoguesJsonNotFoundException exception = new PoguesJsonNotFoundException("poguesId");
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(apiError);
        ResponseEntity<ApiError> response = handler.handlePoguesJsonNotFoundException(exception, request);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandleLunaticJsonNotFoundExceptionReturnCorrectApiError() {
        LunaticJsonNotFoundException exception = new LunaticJsonNotFoundException("poguesId", Context.HOUSEHOLD, Mode.CAWI);
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND.value(), "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(apiError);
        ResponseEntity<ApiError> response = handler.handleLunaticJsonNotFoundException(exception, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandleApiExceptionReturnCorrectApiError() {
        ApiException exception = new ApiException(HttpStatus.NOT_FOUND.value(), "message");
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiError apiError = new ApiError(exception.getStatusCode(), "/", exception.getMessage(), Calendar.getInstance().getTime());

        when(errorComponent.buildApiErrorObject(request, status, "message")).thenReturn(apiError);

        ResponseEntity<ApiError> response = handler.handleApiException(exception, request);
        assertEquals(exception.getStatusCode(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    private void simulateProcessException(ApiError apiError) {
        when(errorComponent.buildApiErrorObject(eq(request), any(), any())).thenReturn(apiError);
    }
}
