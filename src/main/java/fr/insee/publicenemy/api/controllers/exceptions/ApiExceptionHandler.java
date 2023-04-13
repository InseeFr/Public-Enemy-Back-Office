package fr.insee.publicenemy.api.controllers.exceptions;

import fr.insee.publicenemy.api.application.exceptions.ApiException;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiError;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiErrorWithFields;
import fr.insee.publicenemy.api.controllers.exceptions.dto.ApiFieldError;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.LunaticJsonNotFoundException;
import fr.insee.publicenemy.api.infrastructure.ddi.exceptions.PoguesJsonNotFoundException;
import fr.insee.publicenemy.api.infrastructure.i18n.I18nMessageServiceImpl;
import fr.insee.publicenemy.api.infrastructure.questionnaire.RepositoryEntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
/**
 * Handle API exceptions for project
 * Do not work on exceptions occuring before/outside controllers scope
 */
public class ApiExceptionHandler {

    private final fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent errorComponent;

    private final I18nMessageServiceImpl messageService;

    public ApiExceptionHandler(ApiExceptionComponent errorComponent, I18nMessageServiceImpl messageService) {
        this.errorComponent = errorComponent;
        this.messageService = messageService;
    }

    private static final String INTERNAL_EXCEPTION_KEY = "exception.internal";
    private static final String VALIDATION_EXCEPTION_KEY = "exception.validation";
    private static final String NOTFOUND_EXCEPTION_KEY = "exception.notfound";
    private static final String EXCEPTION_OCCURRED_KEY = "exception.occurred";

    /**
     * Global method to process the catched exception
     *
     * @param ex         Exception catched
     * @param statusCode status code linked with this exception
     * @param request    request initiating the exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, int statusCode, WebRequest request) {
        return processException(ex, HttpStatus.valueOf(statusCode), request);
    }

    /**
     * Global method to process the catched exception
     *
     * @param ex      Exception catched
     * @param status  status linked with this exception
     * @param request request initiating the exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, HttpStatus status, WebRequest request) {
        return processException(ex, status, request, null);
    }

    /**
     * Global method to process the catched exception
     *
     * @param ex                   Exception catched
     * @param status               status linked with this exception
     * @param request              request initiating the exception
     * @param overrideErrorMessage message overriding default error message from exception
     * @return the apierror object with associated status code
     */
    private ResponseEntity<ApiError> processException(Exception ex, HttpStatus status, WebRequest request, String overrideErrorMessage) {
        log.error(messageService.getMessage(EXCEPTION_OCCURRED_KEY), ex);
        String errorMessage = ex.getMessage();
        if (overrideErrorMessage != null) {
            errorMessage = overrideErrorMessage;
        }
        ApiError error = errorComponent.buildApiErrorObject(request, status, errorMessage);
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handle Service Exceptions
     *
     * @param ex      DdiException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<ApiError> handleServiceException(
            ServiceException ex,
            WebRequest request) {
        return processException(ex, ex.getStatus().value(), request);
    }

    /**
     * Handle JSON pogues empty exception
     *
     * @param ex      PoguesJsonNotFoundException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({PoguesJsonNotFoundException.class})
    public ResponseEntity<ApiError> handlePoguesJsonNotFoundException(
            PoguesJsonNotFoundException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handle JSON Lunatic empty exception
     *
     * @param ex      PoguesJsonNotFoundException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({LunaticJsonNotFoundException.class})
    public ResponseEntity<ApiError> handleLunaticJsonNotFoundException(
            LunaticJsonNotFoundException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Handle API Exception. .
     *
     * @param ex      API Exception
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({ApiException.class})
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            WebRequest request) {
        log.error(messageService.getMessage(EXCEPTION_OCCURRED_KEY), ex);
        return processException(ex, ex.getStatusCode(), request);
    }

    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required'
     * request parameter is missing.
     *
     * @param ex      MissingServletRequestParameterException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is
     * invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ApiError> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            WebRequest request) {
        return processException(ex, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid
     * validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid
     *                validation fails
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiError handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        log.error(messageService.getMessage(EXCEPTION_OCCURRED_KEY), ex);

        List<ApiFieldError> errors = new ArrayList<>();
        List<String> messages = new ArrayList<>();

        for (ObjectError bindingError : ex.getBindingResult().getGlobalErrors()) {
            messages.add(messageService.getMessage(bindingError));
        }

        for (FieldError bindingError : ex.getBindingResult().getFieldErrors()) {
            ApiFieldError fieldError = new ApiFieldError(bindingError.getField(),
                    messageService.getMessage(bindingError));
            errors.add(fieldError);
        }

        return errorComponent.buildApiErrorWithFields(request, HttpStatus.BAD_REQUEST,
                messageService.getMessage(VALIDATION_EXCEPTION_KEY), errors);
    }

    /**
     * Handles jakarta.validation.ConstraintViolationException.Thrown when @Validated
     * fails.
     *
     * @param ex      the ConstraintViolationException
     * @param request WebRequest object
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    protected ApiErrorWithFields handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            WebRequest request) {
        log.error(messageService.getMessage(EXCEPTION_OCCURRED_KEY), ex);

        List<ApiFieldError> violations = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            violations.add(new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()));
        }

        return errorComponent.buildApiErrorWithFields(request, HttpStatus.BAD_REQUEST,
                messageService.getMessage(VALIDATION_EXCEPTION_KEY), violations);
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is
     * malformed.
     *
     * @param ex      HttpMessageNotReadableException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex      HttpMessageNotWritableException
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    protected ResponseEntity<ApiError> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
                                                                    WebRequest request) {
        return processException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle NoHandlerFoundException.
     *
     * @param ex      NoHandlerFoundException
     * @param request WebRequest object
     * @return the ApiError object
     */

    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ApiError> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request, messageService.getMessage(NOTFOUND_EXCEPTION_KEY));
    }

    /**
     * Handle jakarta.persistence.EntityNotFoundException
     *
     * @param ex      EntityNotFoundException
     * @param request WebRequest object
     * @return the ApiError object
     */
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    protected ResponseEntity<ApiError> handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex,
                                                            WebRequest request) {
        return processException(ex, HttpStatus.NOT_FOUND, request, messageService.getMessage(NOTFOUND_EXCEPTION_KEY));
    }

    /**
     * Handle DataIntegrityViolationException, inspects the cause for different DB
     * causes.
     *
     * @param ex      the DataIntegrityViolationException
     * @param request WebRequest object
     * @return the ApiError object
     */
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                    WebRequest request) {
        log.error(messageService.getMessage(EXCEPTION_OCCURRED_KEY), ex);
        if (ex.getCause() instanceof ConstraintViolationException) {
            return errorComponent.buildApiErrorObject(request, HttpStatus.CONFLICT,
                    messageService.getMessage(INTERNAL_EXCEPTION_KEY));
        }
        return errorComponent.buildApiErrorObject(request, HttpStatus.INTERNAL_SERVER_ERROR,
                messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param ex      the Exception
     * @param request WebRequest object
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                        WebRequest request) {
        return processException(ex, HttpStatus.BAD_REQUEST, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiError> handleMissingPathVariableException(WebRequest request, MissingPathVariableException ex) {
        return processException(ex, HttpStatus.BAD_REQUEST, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle Exception. .
     *
     * @param ex      Exception
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({Exception.class})
    protected ResponseEntity<ApiError> handleException(
            Exception ex,
            WebRequest request) {
        return processException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, messageService.getMessage(INTERNAL_EXCEPTION_KEY));
    }

    /**
     * Handle RepositoryEntity not found Exception. .
     *
     * @param ex      Exception
     * @param request WebRequest object WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler({RepositoryEntityNotFoundException.class})
    public ResponseEntity<ApiError> handleRepositoryEntityNotFoundException(
            RepositoryEntityNotFoundException ex,
            WebRequest request) {
        ApiError error = errorComponent.buildApiErrorObject(request, HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}