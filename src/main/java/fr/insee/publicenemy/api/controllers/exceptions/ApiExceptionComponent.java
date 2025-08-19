package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.controllers.exceptions.dto.InterrogationError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.*;
import lombok.NonNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Component used to build APIError objects
 */
@Component
public class ApiExceptionComponent {

    private final ErrorAttributes errorAttributes;

    public ApiExceptionComponent(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     *
     * @param request origin request
     * @param status status from exception
     * @return error object used for JSON response
     */
    public ApiError buildApiErrorObject(WebRequest request, HttpStatus status) {
        return buildApiErrorObject(request, status, null);
    }

    /**
     *
     * @param request origin request
     * @param status status from exception
     * @param errorMessage error message
     * @return error object used for JSON response
     */
    public ApiError buildApiErrorObject(WebRequest request, HttpStatus status, String errorMessage) {
        String path = getPath(request);
        Date timestamp = getTimeStamp(request);
        return new ApiError(status, path, timestamp,  errorMessage);
    }

    /**
     *
     * @param status status from exception
     * @param errorMessage error message
     * @param errors fields errors objects
     * @return error object used for JSON response
     */
    public ApiErrorWithFields buildApiErrorWithFields(WebRequest request, HttpStatus status,
                                                      String errorMessage, @NonNull List<ApiFieldError> errors) {
        Date timestamp = getTimeStamp(request);
        String path = getPath(request);
        return new ApiErrorWithFields(status, path, timestamp, errorMessage, errors);
    }

    /**
     *
     * @param request request origin
     * @param errors fields errors objects
     * @return error object used for JSON response
     */
    public ApiErrorWithInterrogations buildApiErrorWithInterrogations(WebRequest request, int code, String errorMessage,
                                                                      @NonNull List<InterrogationError> errors) {
        String path = getPath(request);
        Date timestamp = getTimeStamp(request);
        return new ApiErrorWithInterrogations(code, path, timestamp, errorMessage, errors);
    }

    /**
     *
     * @param request web request origin
     * @param errorMessage error message
     * @param errors fields errors objects
     * @return error object used for JSON response
     */
    public ApiErrorWithMessages buildApiErrorWithMessages(WebRequest request, int code, String errorMessage, @NonNull List<String> errors) {
        String path = getPath(request);
        Date timestamp = getTimeStamp(request);
        return new ApiErrorWithMessages(code, path, timestamp, errorMessage, errors);
    }


    /**
     * @param request origin request
     * @return get timestamp from error attributes
     */
    private Date getTimeStamp(WebRequest request) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        return ((Date) attributes.get("timestamp"));
    }

    /**
     *
     * @param request origin request
     * @return get path from origin request
     */
    private String getPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}