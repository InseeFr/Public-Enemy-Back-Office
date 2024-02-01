package fr.insee.publicenemy.api.infrastructure.csv;

public record SurveyUnitStateData(String currentPage, Long date, String state) {

    /**
     * @return a default initial state for survey unit
     */
    public static SurveyUnitStateData createInitialStateData() {
        return new SurveyUnitStateData(null, null, "INIT");
    }
}
