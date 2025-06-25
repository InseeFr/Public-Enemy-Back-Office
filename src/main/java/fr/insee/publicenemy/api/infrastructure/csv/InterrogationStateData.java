package fr.insee.publicenemy.api.infrastructure.csv;

public record InterrogationStateData(String currentPage, Long date, String state) {

    /**
     * @return a default initial state for survey unit
     */
    public static InterrogationStateData createInitialStateData() {
        return null;
    }
}
