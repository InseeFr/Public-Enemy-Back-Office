package fr.insee.publicenemy.api.infrastructure.csv.exceptions;

public class SurveyUnitCsvNotFoundException extends RuntimeException {
    public SurveyUnitCsvNotFoundException(String message) {
        super(message);
    }
}
