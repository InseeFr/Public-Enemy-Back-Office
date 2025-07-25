package fr.insee.publicenemy.api.infrastructure.interro;

import java.time.Instant;

public record InterrogationStateData(String currentPage, Long date, String state) {

    public static final String DEFAULT_PAGE = "1";
    public static final String DEFAULT_STATE = "INIT";
    public static Long getDefaultDate(){
        return Instant.now().toEpochMilli();
    }

    /**
     * @return a default initial state for survey unit, state can't be null
     */
    public static InterrogationStateData createInitialStateData() {
        return null;
    }
}
