package fr.insee.publicenemy.api.application.domain.model;

import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;

import java.util.Objects;

public record PersonalizationMapping(String interrogationId, Long questionnaireId, Mode mode, int dataIndex) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonalizationMapping that = (PersonalizationMapping) o;
        return dataIndex == that.dataIndex && mode == that.mode && Objects.equals(questionnaireId, that.questionnaireId) && Objects.equals(interrogationId, that.interrogationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interrogationId, questionnaireId, mode, dataIndex);
    }

    public String getQuestionnaireModelId(){
        return IdentifierGenerationUtils.generateCampaignAndQuestionnaireModelIdentifier(questionnaireId, mode);
    }
}
