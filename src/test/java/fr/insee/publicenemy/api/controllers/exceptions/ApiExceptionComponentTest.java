package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionComponentTest {

    private ApiExceptionComponent component;

    @Mock
    private ServletWebRequest request;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ErrorAttributes errorAttributes;

    Map<String, Object> attributes;

    private final Date currentDate = Calendar.getInstance().getTime();

    @BeforeEach
    void init() {
        attributes = new HashMap<>();
        attributes.put("timestamp", currentDate);
        component = new ApiExceptionComponent(errorAttributes);
        when(request.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/");
        when(errorAttributes.getErrorAttributes(eq(request), any())).thenReturn(attributes);
    }

    @Test
    void onBuildErrorObjectReturnApiError() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildApiErrorObject(request, status, "errorMessage");
        assertEquals(currentDate, apiError.getTimestamp());
        assertEquals("/", apiError.getPath());
        assertTrue(apiError.getMessage().contains("errorMessage"));
        assertEquals(status.value(), apiError.getCode());
    }

    @Test
    void onBuildErrorObjectWithApiExceptionReturnApiError() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildApiErrorObject(request, status);
        assertEquals(currentDate, apiError.getTimestamp());
        assertEquals("/", apiError.getPath());
        assertEquals(status.value(), apiError.getCode());
    }

    @Test
    void onBuildErrorObjectWhenExceptionNullReturnApiError() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildApiErrorObject(request, status, "errorMessage");
        assertEquals(currentDate, apiError.getTimestamp());
        assertEquals("/", apiError.getPath());
        assertTrue(apiError.getMessage().contains("errorMessage"));
        assertEquals(status.value(), apiError.getCode());
    }

    @Test
    void onBuildErrorObjectWithEmptyErrorMessageReturnApiErrorWithHttpStatusErrorMessage() {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildApiErrorObject(request, status);
        assertTrue(apiError.getMessage().contains(status.getReasonPhrase()));
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    void onBuildErrorObjectWithEmptyErrorMessageReturnApiErrorWithHttpStatusErrorMessage(String errorMessage) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError apiError = component.buildApiErrorObject(request, status, errorMessage);
        assertTrue(apiError.getMessage().contains(status.getReasonPhrase()));
    }
}
