package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseQuestionnaireEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="questionnaire_pogues_id")
    @NotNull
    private String questionnaireId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", referencedColumnName = "id")
    private CampaignEntity campaign;

    @Column
    @NotNull
    private String label;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "context_id", referencedColumnName = "id")
    @NotNull
    private ContextEntity context;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "questionnaire_mode", 
        joinColumns = @JoinColumn(name = "questionnaire_id"), 
        inverseJoinColumns = @JoinColumn(name = "mode_id"))
    @NotNull
    private List<ModeEntity> modes;

    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    BaseQuestionnaireEntity(String questionnaireId, CampaignEntity campaign, String label, ContextEntity context, List<ModeEntity> modes) {
        this.questionnaireId = questionnaireId;
        this.label = label;
        this.context = context;
        this.modes = modes;
        this.campaign = campaign;
    }

    public Questionnaire toModel() {
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(getId());
        questionnaire.setQuestionnaireId(getQuestionnaireId());
        questionnaire.setLabel(getLabel());
        questionnaire.setContext(getContext().toModel());
        List<Mode> modesQuestionnaire = getModes().stream().map(modeEntity -> modeEntity.toModel()).collect(Collectors.toList());
        questionnaire.setModes(modesQuestionnaire);
        questionnaire.setUpdatedDate(getUpdatedDate());
        return questionnaire;
    }

    public static BaseQuestionnaireEntity createFromModel(String campaignLabel, Questionnaire questionnaire) {
        ContextEntity context = ContextEntity.createFromModel(questionnaire.getContext());
        List<ModeEntity> modesQuestionnaire = questionnaire.getModes().stream().map(ModeEntity::createFromModel).collect(Collectors.toList());
        CampaignEntity campaign = CampaignEntity.createWithLabel(campaignLabel);
        BaseQuestionnaireEntity questionnaireEntity = new BaseQuestionnaireEntity(questionnaire.getQuestionnaireId(), campaign, questionnaire.getLabel(),context, modesQuestionnaire);
        Date date = Calendar.getInstance().getTime();
    
        questionnaireEntity.setCreationDate(date);
        questionnaireEntity.setUpdatedDate(date);
        return questionnaireEntity;
    }
}
