package fr.insee.publicenemy.api.controllers.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import fr.insee.publicenemy.api.application.dto.ApiError;
import fr.insee.publicenemy.api.application.exceptions.ApiException;

/**
 * Component used to build API errors objects from exceptions
 */
@Component
public class ApiExceptionComponent {

    /**
     * 
     * @param attributes
     * @param request source request of when exception ocurred
     * @param status Exception status
     * @return get the API Error
     */
    public ApiError getApiError(Map<String, Object> attributes, WebRequest request, HttpStatus status) {
        return getApiError(attributes, request, status, null);
    }

    /**
     * 
     * @param attributes
     * @param request
     * @param status
     * @param ex
     * @return
     */
    public ApiError getApiError(Map<String, Object> attributes, WebRequest request, HttpStatus status,
            Exception ex) {
        return getApiError(attributes, request, status, ex, null);
    }

    public ApiError getApiError(Map<String, Object> attributes, WebRequest request, HttpStatus status,
            Exception ex, String errorMessage) {

        ApiError errorObject = new ApiError();

        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        errorObject.setTimestamp((Date) attributes.get("timestamp"));
        errorObject.setPath(path);
        errorObject.setStatus(status.value());

        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = status.getReasonPhrase();
        }
        errorObject.addMessage(errorMessage);
        // Ajout de l'exception
        if (ex != null) {
            StringWriter stackTrace = new StringWriter();
            ex.printStackTrace(new PrintWriter(stackTrace));
            stackTrace.flush();

            errorObject.setStackTrace(stackTrace.toString());
            errorObject.setExceptionName(ex.getClass().getName());
            errorObject.setDebugMessage(ex.getLocalizedMessage());
        }

        return errorObject;
    }

    public ApiError getApiError(Map<String, Object> attributes, WebRequest request, ApiException ex) {
        ApiError errorObject = getApiError(attributes, request, ex.getStatus(), ex);
        errorObject.setFieldErrors(ex.getErrors());
        errorObject.setMessages(ex.getMessages());
        return errorObject;
    }
}
