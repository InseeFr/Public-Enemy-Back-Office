package fr.insee.publicenemy.api.application.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    @NotNull
    private String label;
    @NotNull
    private Context context;
    @NotNull
    private List<QuestionnaireMode> questionnaireModes;
    @NotNull
    private byte[] surveyUnitData;
    private boolean isSynchronized;

    public Questionnaire(String poguesId, String label, Context context, List<Mode> modes, byte[] surveyUnitData) {
        this.poguesId = poguesId;
        this.label = label;
        this.context = context;
        this.questionnaireModes = QuestionnaireMode.toModel(modes);
        this.surveyUnitData = surveyUnitData;
        this.isSynchronized = false;
    }

    public Questionnaire(Long id, Context context, byte[] surveyUnitData) {
        this.id = id;
        this.context = context;
        this.surveyUnitData = surveyUnitData;
        this.isSynchronized = false;
    }

    public Questionnaire(String poguesId, String label, List<Mode> modes) {
        this.poguesId = poguesId;
        this.label = label;
        this.questionnaireModes = QuestionnaireMode.toModel(modes);
        this.isSynchronized = false;
    }
    public Questionnaire(Ddi ddi, Context context, byte[] surveyUnitData) {
        this.poguesId = ddi.poguesId();
        this.label = ddi.label();
        this.context = context;
        this.questionnaireModes = QuestionnaireMode.toModel(ddi.modes());
        this.surveyUnitData = surveyUnitData;
        this.isSynchronized = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Questionnaire that = (Questionnaire) o;
        return isSynchronized == that.isSynchronized
                && Objects.equals(id, that.id)
                && Objects.equals(poguesId, that.poguesId)
                && Objects.equals(label, that.label) && context == that.context
                && Objects.equals(questionnaireModes, that.questionnaireModes)
                && Arrays.equals(surveyUnitData, that.surveyUnitData);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, poguesId, label, context, questionnaireModes, isSynchronized);
        result = 31 * result + Arrays.hashCode(surveyUnitData);
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
                    ", surveyUnitData=" + Arrays.toString(surveyUnitData) +
                    ", isSynchronized=" + isSynchronized +
                    '}';
        }
}
