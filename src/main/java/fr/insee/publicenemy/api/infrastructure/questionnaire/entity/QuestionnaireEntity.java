package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.infrastructure.questionnaire.RepositoryEntityNotFoundException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questionnaire")
public class QuestionnaireEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "questionnaire_pogues_id")
    @NotNull
    private String poguesId;

    @Column
    @NotNull
    private String label;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "context")
    private Context context;

    @OneToMany(mappedBy = "questionnaire", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionnaireModeEntity> modeEntities;

    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "survey_unit_data")
    @NotNull
    private byte[] interrogationData;

    @Column(name = "synchronized", nullable = false)
    private boolean isSynchronized;

    /**
     * Constructor
     *
     * @param poguesId           questionnaire pogues id
     * @param label              questionnaire label
     * @param context            insee context
     * @param questionnaireModes questionnaire modes
     * @param interrogationData     csv interrogation data
     * @param isSynchronized     is this questionnaire full synchronized with orchestrator
     */
    public QuestionnaireEntity(String poguesId, String label, Context context, List<QuestionnaireMode> questionnaireModes,
                               @NotNull byte[] interrogationData, boolean isSynchronized) {
        Date date = Calendar.getInstance().getTime();
        this.poguesId = poguesId;
        this.label = label;
        this.context = context;
        this.modeEntities = QuestionnaireModeEntity.fromModel(this, questionnaireModes);
        this.creationDate = date;
        this.updatedDate = date;
        this.interrogationData = interrogationData;
        this.isSynchronized = isSynchronized;
    }

    /**
     * @return application model of this questionnaire
     */
    public Questionnaire toModel(byte[] interrogationData) {
        return new Questionnaire(getId(), getPoguesId(), getLabel(),
                getContext(), QuestionnaireModeEntity.toModel(modeEntities),
                interrogationData,
                isSynchronized());
    }

    /**
     * @return application model of this questionnaire
     */
    public Questionnaire toModel() {
        return new Questionnaire(getId(), getPoguesId(), getLabel(),
                getContext(), QuestionnaireModeEntity.toModel(modeEntities), null,
                isSynchronized());
    }

    /**
     * Permits to create the entity before saving it to persistence unit
     *
     * @param questionnaire application model of questionnaire
     * @return the entity representation of the questionnaire
     */
    public static QuestionnaireEntity createEntity(@NonNull Questionnaire questionnaire) {
        return new QuestionnaireEntity(questionnaire.getPoguesId(), questionnaire.getLabel(),
                questionnaire.getContext(), questionnaire.getQuestionnaireModes(), questionnaire.getInterrogationData(), false);
    }

    /**
     * Update questionnaire entity from questionnaire
     *
     * @param questionnaire questionnaire to update
     */
    public void update(@NonNull Questionnaire questionnaire) {
        byte[] questionnaireUnitData = questionnaire.getInterrogationData();
        if (questionnaireUnitData != null && questionnaireUnitData.length > 0) {
            setInterrogationData(questionnaireUnitData);
        }
        setContext(questionnaire.getContext());
        setLabel(questionnaire.getLabel());
        setUpdatedDate(Calendar.getInstance().getTime());
        List<QuestionnaireModeEntity> qModeEntities =
                QuestionnaireModeEntity.fromModel(this, questionnaire.getQuestionnaireModes());
        // need to create a mutable list from the immutable one or jpa fails on merge lists
        setModeEntities(new ArrayList<>(qModeEntities));
        setSynchronized(questionnaire.isSynchronized());
    }

    /**
     * Update synchronisation state for the questionnaire entity
     *
     * @param questionnaire with synchronisation state
     */
    public void updateState(@NotNull Questionnaire questionnaire) {
        this.isSynchronized = questionnaire.isSynchronized();
        questionnaire.getQuestionnaireModes()
                .forEach(questionnaireMode -> {
                    QuestionnaireModeEntity questionnaireModeEntity = getQuestionnaireModeEntity(questionnaireMode.getMode());
                    questionnaireModeEntity.setSynchronisationState(questionnaireMode.getSynchronisationState());
                });

    }

    /**
     * Get questionnaire mode entity associated with the corresponding mode
     *
     * @param mode insee mode
     * @return the questionnaire mode entity
     */
    private QuestionnaireModeEntity getQuestionnaireModeEntity(@NotNull Mode mode) {
        return this.modeEntities.stream()
                .filter(modeEntity -> modeEntity.getMode().equals(mode))
                .findFirst()
                .orElseThrow(() -> new RepositoryEntityNotFoundException(
                        String.format("Mode %s not found in entity %s", mode.name(), this.getId()))
                );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionnaireEntity that = (QuestionnaireEntity) o;
        return Objects.equals(id, that.id)
                && Objects.equals(poguesId, that.poguesId)
                && Objects.equals(label, that.label)
                && context == that.context
                && Objects.equals(modeEntities, that.modeEntities)
                && Objects.equals(creationDate, that.creationDate)
                && Objects.equals(updatedDate, that.updatedDate)
                && Arrays.equals(interrogationData, that.interrogationData)
                && Objects.equals(isSynchronized, that.isSynchronized);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, poguesId, label, context, modeEntities, creationDate, updatedDate, isSynchronized);
        result = 31 * result + Arrays.hashCode(interrogationData);
        return result;
    }

    @Override
    public String toString() {
        return "QuestionnaireEntity{" +
                "id=" + id +
                ", poguesId='" + poguesId + '\'' +
                ", label='" + label + '\'' +
                ", context=" + context +
                ", modeEntities=" + modeEntities +
                ", creationDate=" + creationDate +
                ", updatedDate=" + updatedDate +
                ", interrogationData=" + Arrays.toString(interrogationData) +
                ", isSynchronized='" + isSynchronized + '\'' +
                '}';
    }
}
