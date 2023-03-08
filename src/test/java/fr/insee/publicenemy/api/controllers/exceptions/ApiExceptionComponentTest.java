package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiFieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionComponentTest {

    private ApiExceptionComponent component;

    @Mock
    private ServletWebRequest request;

    @Mock
    private HttpServletRequest httpServletRequest;

    Map<String, Object> attributes;

    private final Date currentDate = Calendar.getInstance().getTime();

    @BeforeEach
    void init() {
        component = new ApiExceptionComponent();
        when(request.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/");
        attributes = new HashMap<>();

        attributes.put("timestamp", currentDate);
    }

    @Test
    void onBuildErrorObjectReturnApiError() {
        Exception ex = new Exception("message");
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildErrorObject(attributes, request, status, ex, "errorMessage");
        assertEquals(currentDate, apiError.timestamp());
        assertEquals("/", apiError.path());
        assertTrue(apiError.messages().contains("errorMessage"));
        assertEquals(ex.getMessage(), apiError.debugMessage());
        assertEquals(status.value(), apiError.status());
    }

    @Test
    void onBuildErrorObjectWithApiExceptionReturnApiError() {
        List<ApiFieldError> fieldsErrors = new ArrayList<>();
        ApiFieldError fieldError = new ApiFieldError("field", "field-message");
        fieldsErrors.add(fieldError);
        ApiException ex = new ApiException(404, "errorMessage", fieldsErrors);
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildErrorObject(attributes, request, ex);
        assertEquals(currentDate, apiError.timestamp());
        assertEquals("/", apiError.path());
        assertEquals(ex.getMessage(), apiError.debugMessage());
        assertEquals(status.value(), apiError.status());
        List<ApiFieldError> apiFields = apiError.fieldErrors();
        assertEquals(fieldError, apiFields.get(0));

    }

    @Test
    void onBuildErrorObjectWhenExceptionNullReturnApiError() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildErrorObject(attributes, request, status, null, "errorMessage");
        assertEquals(currentDate, apiError.timestamp());
        assertEquals("/", apiError.path());
        assertTrue(apiError.messages().contains("errorMessage"));
        assertEquals(status.value(), apiError.status());
    }

    @Test
    void onBuildErrorObjectWithEmptyErrorMessageReturnApiErrorWithHttpStatusErrorMessage() {
        Exception ex = new Exception("message");
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildErrorObject(attributes, request, status, ex);
        assertTrue(apiError.messages().contains(status.getReasonPhrase()));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void onBuildErrorObjectWithEmptyErrorMessageReturnApiErrorWithHttpStatusErrorMessage(String errorMessage) {
        Exception ex = new Exception("message");
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildErrorObject(attributes, request, status, ex, errorMessage);
        assertTrue(apiError.messages().contains(status.getReasonPhrase()));
    }


}
