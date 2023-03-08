package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.LunaticJsonNotFoundException;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.PoguesJsonNotFoundException;
import fr.insee.publicenemy.api.infrastructure.i18n.I18nMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {
    private ApiExceptionHandler handler;

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private ApiExceptionComponent exceptionComponent;

    @Mock
    private I18nMessageServiceImpl messageService;

    @Mock
    private WebRequest request;

    @BeforeEach
    public void init() {
        handler = new ApiExceptionHandler(exceptionComponent, errorAttributes, messageService);
    }

    @Test
    void onHandleServiceExceptionReturnCorrectApiError() {
        ServiceException exception = new ServiceException(404, "message");
        ApiError apiError = new ApiError(exception.getCode(), "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(exception, apiError);
        ResponseEntity<ApiError> response = handler.handleServiceException(exception, request);
        assertEquals(exception.getCode(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandlePoguesJsonNotFoundExceptionReturnCorrectApiError() {
        PoguesJsonNotFoundException exception = new PoguesJsonNotFoundException("poguesId");
        ApiError apiError = new ApiError(404, "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(exception, apiError);
        ResponseEntity<ApiError> response = handler.handlePoguesJsonNotFoundException(exception, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandleLunaticJsonNotFoundExceptionReturnCorrectApiError() {
        LunaticJsonNotFoundException exception = new LunaticJsonNotFoundException("poguesId", Context.HOUSEHOLD, Mode.CAWI);
        ApiError apiError = new ApiError(404, "/", exception.getMessage(), Calendar.getInstance().getTime());
        simulateProcessException(exception, apiError);
        ResponseEntity<ApiError> response = handler.handleLunaticJsonNotFoundException(exception, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    @Test
    void onHandleApiExceptionReturnCorrectApiError() {
        ApiException exception = new ApiException(404, "message");
        ApiError apiError = new ApiError(exception.getStatusCode(), "/", exception.getMessage(), Calendar.getInstance().getTime());
        Map<String, Object> attributes = new HashMap<>();

        when(errorAttributes.getErrorAttributes(eq(request), any())).thenReturn(attributes);
        when(exceptionComponent.buildErrorObject(attributes, request, exception)).thenReturn(apiError);

        ResponseEntity<ApiError> response = handler.handleApiException(exception, request);
        assertEquals(exception.getStatusCode(), response.getStatusCode().value());
        assertEquals(apiError, response.getBody());
    }

    private void simulateProcessException(Exception exception, ApiError apiError) {
        Map<String, Object> attributes = new HashMap<>();
        when(errorAttributes.getErrorAttributes(eq(request), any())).thenReturn(attributes);
        when(exceptionComponent.buildErrorObject(eq(attributes), eq(request), any(), eq(exception), eq(exception.getMessage()))).thenReturn(apiError);
    }
}
