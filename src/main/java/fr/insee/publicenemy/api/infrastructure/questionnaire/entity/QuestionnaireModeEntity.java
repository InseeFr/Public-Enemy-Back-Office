package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity(name = "questionnaire_mode")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(QuestionnaireModeEntityPK.class)
public class QuestionnaireModeEntity implements Serializable {
    @Id
    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private Mode mode;

    @Id
    @ManyToOne
    @JoinColumn(name = "questionnaire_id")
    private QuestionnaireEntity questionnaire;

    @Column(name = "state")
    private String synchronisationState;

    /**
     * Constructor
     *
     * @param questionnaire        the questionnaire linked to this questionnaire mode
     * @param mode                 insee mode
     * @param synchronisationState synchronisation state of this mode in orchestrators
     */
    public QuestionnaireModeEntity(QuestionnaireEntity questionnaire, Mode mode, String synchronisationState) {
        this.mode = mode;
        this.questionnaire = questionnaire;
        this.synchronisationState = synchronisationState;
    }

    /**
     * @param modeEntity mode entity
     * @return the application model of this entity
     */
    public static QuestionnaireMode toModel(QuestionnaireModeEntity modeEntity) {
        return new QuestionnaireMode(modeEntity.getQuestionnaire().getId(),
                modeEntity.getMode(),
                modeEntity.getSynchronisationState());
    }

    /**
     * @param modeEntities mode entities
     * @return the application model of these entities
     */
    public static List<QuestionnaireMode> toModel(List<QuestionnaireModeEntity> modeEntities) {
        return modeEntities.stream().map(QuestionnaireModeEntity::toModel).toList();
    }

    /**
     * Permits to create a list of mode entities before saving it to persistence unit
     *
     * @param questionnaireEntity the questionnaire entity to link to the mode entity
     * @param questionnaireModes  list of application mode model
     * @return the questionnaire mode entity from the questionnaire entity and mode
     */
    public static List<QuestionnaireModeEntity> fromModel(QuestionnaireEntity questionnaireEntity, List<QuestionnaireMode> questionnaireModes) {
        return questionnaireModes.stream()
                .map(questionnaireMode -> QuestionnaireModeEntity.fromModel(questionnaireEntity, questionnaireMode))
                .toList();
    }

    /**
     * Permits to create a mode entity before saving it to persistence unit
     *
     * @param questionnaireEntity the questionnaire entity to link to the mode entity
     * @param questionnaireMode   application mode model
     * @return the questionnaire mode entity from the questionnaire entity and mode
     */
    public static QuestionnaireModeEntity fromModel(QuestionnaireEntity questionnaireEntity, QuestionnaireMode questionnaireMode) {
        return new QuestionnaireModeEntity(questionnaireEntity, questionnaireMode.getMode(), questionnaireMode.getSynchronisationState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireModeEntity that = (QuestionnaireModeEntity) o;
        return mode == that.mode && Objects.equals(questionnaire, that.questionnaire) && Objects.equals(synchronisationState, that.synchronisationState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, questionnaire.getId(), synchronisationState);
    }

    @Override
    public String toString() {
        return "QuestionnaireModeEntity{" +
                "mode=" + mode +
                ", questionnaire=" + questionnaire.getId() +
                ", synchronisationState='" + synchronisationState + '\'' +
                '}';
    }
}