package fr.insee.publicenemy.api.controllers.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import fr.insee.publicenemy.api.application.dto.ApiError;
import fr.insee.publicenemy.api.application.dto.ApiFieldError;
import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.infrastructure.i18n.I18nMessageServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle all kinds of exceptions. Only works when request is going through controllers
 * 
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @Autowired
    private ApiExceptionComponent errorComponent;

    @Autowired
    private ErrorAttributes errorAttributes;

    @Autowired
    private I18nMessageServiceImpl messageService;

    private static final String INTERNAL_EXCEPTION_KEY = "exception.internal";
    private static final String VALIDATION_EXCEPTION_KEY = "exception.validation";
    private static final String NOTFOUND_EXCEPTION_KEY = "exception.notfound";

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required'
     * request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ApiError handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.BAD_REQUEST, ex, null);
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is
     * invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ApiError handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid
     * validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid
     *                validation fails
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiError handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        ApiError apiError = getApiError(request, HttpStatus.BAD_REQUEST, ex,
                messageService.getMessage(VALIDATION_EXCEPTION_KEY));

        List<ApiFieldError> errors = new ArrayList<>();

        List<String> messages = apiError.getMessages();
        for (ObjectError bindingError : ex.getBindingResult().getGlobalErrors()) {
            messages.add(messageService.getMessage(bindingError));
        }

        for (FieldError bindingError : ex.getBindingResult().getFieldErrors()) {
            ApiFieldError fieldError = new ApiFieldError(bindingError.getField(),
                    messageService.getMessage(bindingError));
            errors.add(fieldError);
        }

        apiError.setFieldErrors(errors);

        return apiError;
    }

    /**
     * Handles javax.validation.ConstraintViolationException.Thrown when @Validated
     * fails.
     *
     * @param ex      the ConstraintViolationException
     * @param request
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    protected ApiError handleConstraintViolation(
            javax.validation.ConstraintViolationException ex,
            WebRequest request) {
        ApiError error = getApiError(request, HttpStatus.BAD_REQUEST, ex,
                messageService.getMessage(VALIDATION_EXCEPTION_KEY));
        List<ApiFieldError> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.add(new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        error.setFieldErrors(violations);
        return error;
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is
     * malformed.
     *
     * @param ex      HttpMessageNotReadableException
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ApiError handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        log.info("{} to {}", servletWebRequest.getHttpMethod(), servletWebRequest.getRequest().getServletPath());
        return getApiError(request, HttpStatus.BAD_REQUEST, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex      HttpMessageNotWritableException
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpMessageNotWritableException.class)
    protected ApiError handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.INTERNAL_SERVER_ERROR, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle NoHandlerFoundException.
     *
     * @param ex
     * @param request
     * @return
     */

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ApiError handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        return getApiError(request, HttpStatus.BAD_REQUEST, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle javax.persistence.EntityNotFoundException
     * 
     * @param ex
     * @param request
     * @return
     */
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    protected ApiError handleEntityNotFound(javax.persistence.EntityNotFoundException ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.NOT_FOUND, ex,
                messageService.getMessage(NOTFOUND_EXCEPTION_KEY));
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB
     * causes.
     *
     * @param ex      the DataIntegrityViolationException
     * @param request
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex,
            WebRequest request) {
        ApiError error = getApiError(request, HttpStatus.INTERNAL_SERVER_ERROR, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
        if (ex.getCause() instanceof ConstraintViolationException) {
            error = getApiError(request, HttpStatus.CONFLICT, ex,
                    messageService.getMessage(INTERNAL_EXCEPTION_KEY));
        }
        return error;
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex      the Exception
     * @param request
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ApiError handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.BAD_REQUEST, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingPathVariableException.class)
    public ApiError handleMissingPathVariableException(WebRequest request, MissingPathVariableException ex) {
        return getApiError(request, HttpStatus.BAD_REQUEST, ex, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle Exception. .
     *
     * @param ex      Exception
     * @param request WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    protected ApiError handleException(
            Exception ex,
            WebRequest request) {
        return getApiError(request, HttpStatus.INTERNAL_SERVER_ERROR, ex,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle API Exception. .
     *
     * @param ex      Exception
     * @param request WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({ ApiException.class })
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            WebRequest request) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        ApiError error = errorComponent.getApiError(attributes, request, ex);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    private ApiError getApiError(WebRequest request, HttpStatus status, Exception ex, String message) {
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        return errorComponent.getApiError(attributes, request, status, ex, message);
    }
}