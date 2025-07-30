package fr.insee.publicenemy.api.application.domain.model;

/**
 * State used for synchronisation with queen
 */
public enum PersonalizationState {
    NONE,
    STARTED,
    ERROR,
    COMPLETED;
}
