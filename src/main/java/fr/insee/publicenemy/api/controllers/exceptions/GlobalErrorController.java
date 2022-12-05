package fr.insee.publicenemy.api.controllers.exceptions;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import fr.insee.publicenemy.api.application.dto.ApiError;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Handle exceptions globally (not catched by ApiExceptionHandler)
 */
@RestController
@ApiIgnore
public class GlobalErrorController extends AbstractErrorController {

    @Autowired
    private ApiExceptionComponent errorComponent;

    public GlobalErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @GetMapping(value = "/error")
    public ApiError handleError(HttpServletRequest request, WebRequest webRequest) {
        Map<String, Object> attributes = super.getErrorAttributes(request, ErrorAttributeOptions.defaults());
        HttpStatus status = HttpStatus.resolve((Integer) attributes.get("status"));
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        return errorComponent.getApiError(attributes, webRequest, status, exception);
    }
}
