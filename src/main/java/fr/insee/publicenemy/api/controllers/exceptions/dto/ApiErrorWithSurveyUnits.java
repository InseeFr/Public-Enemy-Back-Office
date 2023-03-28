package fr.insee.publicenemy.api.controllers.exceptions.dto;

import fr.insee.publicenemy.api.controllers.dto.SurveyUnitErrors;
import lombok.NonNull;

import java.util.Date;
import java.util.List;

/**
 * API Error object returned as JSON response to client including survey units errors
 */
public class ApiErrorWithSurveyUnits extends ApiErrorDetails<List<SurveyUnitErrors>> {
    /**
     * @param code error code
     * @param path origin request path
     * @param timestamp timestamp of the generated error
     * @param errorMessage error message
     * @param details specific details about this error
     */
    public ApiErrorWithSurveyUnits(int code, String path, Date timestamp, String errorMessage, @NonNull List<SurveyUnitErrors> details) {
        super(code, path, timestamp, errorMessage, details);
    }
}
