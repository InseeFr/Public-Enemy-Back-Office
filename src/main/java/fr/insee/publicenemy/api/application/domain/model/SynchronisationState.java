package fr.insee.publicenemy.api.application.domain.model;

/**
 * State used for synchronisation with queen
 */
public enum SynchronisationState {
    INIT_QUESTIONNAIRE,
    INIT_CAMPAIGN,
    INIT_SURVEY_UNIT,
    INIT_PERSO_MAPPING,
    OK;
}
