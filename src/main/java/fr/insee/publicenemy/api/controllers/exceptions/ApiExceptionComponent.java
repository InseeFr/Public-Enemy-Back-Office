package fr.insee.publicenemy.api.controllers.exceptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.publicenemy.api.application.dto.ApiError;
import fr.insee.publicenemy.api.application.exceptions.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 *
 * @author vagrant
 */
@Component
public class ApiExceptionComponent {

    private final ObjectMapper objectMapper;

    public ApiExceptionComponent(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ApiError buildErrorObject(Map<String, Object> attributes, WebRequest request, HttpStatus status) {
        return buildErrorObject(attributes, request, status, null);
    }

    public ApiError buildErrorObject(Map<String, Object> attributes, WebRequest request, HttpStatus status,
            Exception ex) {
        return buildErrorObject(attributes, request, status, ex, null);
    }

    public ApiError buildErrorObject(Map<String, Object> attributes, WebRequest request, HttpStatus status,
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

    public ApiError buildErrorObject(Map<String, Object> attributes, WebRequest request, ApiException ex) {
        ApiError errorObject = buildErrorObject(attributes, request, HttpStatus.valueOf(ex.getStatusCode()), ex);
        errorObject.setFieldErrors(ex.getErrors());
        errorObject.addMessage(ex.getMessage());
        return errorObject;
    }

    public void buildErrorResponse(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
            Exception ex, String errorMessage) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> attributs = new HashMap<>();
        attributs.put("timestamp", Calendar.getInstance().getTime());

        ApiError error = buildErrorObject(attributs, webRequest, status, ex, errorMessage);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}

