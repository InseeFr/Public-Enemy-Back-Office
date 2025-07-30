package fr.insee.publicenemy.api.application.domain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;

public record QuestionnaireModel(String poguesId, String versionId, String label, List<Mode> modes, JsonNode content) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireModel questionnaireModel = (QuestionnaireModel) o;
        return Objects.equals(poguesId, questionnaireModel.poguesId)
                && Objects.equals(versionId, questionnaireModel.versionId)
                && Objects.equals(label, questionnaireModel.label)
                && Objects.equals(modes, questionnaireModel.modes)
                && Objects.equals(content, questionnaireModel.content);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(poguesId, versionId, label, modes);
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "QuestionnaireModel{" +
                "poguesId='" + poguesId + '\'' +
                ", versionId='" + versionId + '\'' +
                ", label='" + label + '\'' +
                ", modes=" + modes +
                ", content=" + content +
                '}';
    }
}
