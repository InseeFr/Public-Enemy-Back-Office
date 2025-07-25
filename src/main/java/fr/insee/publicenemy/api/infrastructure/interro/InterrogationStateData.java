package fr.insee.publicenemy.api.infrastructure.interro;

public record InterrogationStateData(String currentPage, Long date, String state) {

    /**
     * @return a default initial state for survey unit, state can't be null
     */
    public static InterrogationStateData createInitialStateData() {
        return null;
    }
}
