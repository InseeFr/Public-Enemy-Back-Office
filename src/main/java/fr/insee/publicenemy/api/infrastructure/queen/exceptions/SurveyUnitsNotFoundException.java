package fr.insee.publicenemy.api.infrastructure.queen.exceptions;

public class SurveyUnitsNotFoundException extends RuntimeException {
    public SurveyUnitsNotFoundException(String campaignId){
        super(String.format("No survey Units for campaign: %s", campaignId));
    }
}
