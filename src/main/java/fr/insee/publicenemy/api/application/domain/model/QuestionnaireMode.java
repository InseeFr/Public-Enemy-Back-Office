package fr.insee.publicenemy.api.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class QuestionnaireMode {

    private Long id;
    private Mode mode;
    private String synchronisationState;

    public QuestionnaireMode(Mode mode) {
        this(null, mode, null);
    }

    public QuestionnaireMode(Mode mode, String synchronisationState) {
        this(null, mode, synchronisationState);
    }

    public static List<QuestionnaireMode> toModel(List<Mode> modes) {
        return modes.stream().map(QuestionnaireMode::new).toList();
    }

    public void setSynchronisationState(String synchronisationState) {
        this.synchronisationState = synchronisationState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireMode that = (QuestionnaireMode) o;
        return Objects.equals(id, that.id) && mode == that.mode && Objects.equals(synchronisationState, that.synchronisationState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mode, synchronisationState);
    }
}
