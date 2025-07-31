package fr.insee.publicenemy.api.application.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class Questionnaire {
    private Long id;
    @NotNull
    private String poguesId;
    private String versionId;
    @NotNull
    private String label;
    @NotNull
    private Context context;
    @NotNull
    private List<QuestionnaireMode> questionnaireModes;
    @NotNull
    private byte[] interrogationData;
    private PersonalizationState personalizationState;
    private boolean isOutdated;

    public Questionnaire(String poguesId, String versionId, String label, Context context, List<Mode> modes, byte[] interrogationData) {
        this.poguesId = poguesId;
        this.versionId = versionId;
        this.label = label;
        this.context = context;
        this.questionnaireModes = QuestionnaireMode.toModel(modes);
        this.interrogationData = interrogationData;
        this.isOutdated = false;
    }

    public Questionnaire(Long id, Context context, byte[] interrogationData) {
        this.id = id;
        this.context = context;
        this.interrogationData = interrogationData;
        this.isOutdated = false;
    }

    // usage pogues
    public Questionnaire(String poguesId, String label, List<Mode> modes) {
        this.poguesId = poguesId;
        this.label = label;
        this.questionnaireModes = QuestionnaireMode.toModel(modes);
        this.isOutdated = false;
        this.personalizationState = PersonalizationState.NONE;
    }

    // usage: addQuestionnaire -> personalizationState STARTED
    public Questionnaire(QuestionnaireModel questionnaireModel, Context context, byte[] interrogationData) {
        this.poguesId = questionnaireModel.poguesId();
        this.versionId = questionnaireModel.versionId();
        this.label = questionnaireModel.label();
        this.context = context;
        this.questionnaireModes = QuestionnaireMode.toModel(questionnaireModel.modes());
        this.interrogationData = interrogationData;
        this.isOutdated = false;
        this.personalizationState = PersonalizationState.STARTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Questionnaire that = (Questionnaire) o;
        return isOutdated == that.isOutdated
                && Objects.equals(id, that.id)
                && Objects.equals(poguesId, that.poguesId)
                && Objects.equals(versionId, that.versionId)
                && Objects.equals(label, that.label) && context == that.context
                && Objects.equals(questionnaireModes, that.questionnaireModes)
                && Objects.equals(personalizationState, that.personalizationState)
                && Arrays.equals(interrogationData, that.interrogationData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, poguesId, versionId, label, context, questionnaireModes, personalizationState, isOutdated);
        result = 31 * result + Arrays.hashCode(interrogationData);
        return result;
    }

    @Override
    public String toString() {
        return "Questionnaire{" +
                "id=" + id +
                ", poguesId='" + poguesId + '\'' +
                ", label='" + label + '\'' +
                ", context=" + context +
                ", questionnaireModes=" + questionnaireModes +
                ", isOutdated=" + isOutdated +
                ", personalizationState=" + personalizationState +
                '}';
    }
}
