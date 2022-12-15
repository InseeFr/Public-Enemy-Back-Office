package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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

    @NotNull
    @Enumerated(EnumType.STRING)    
    @Column(name = "context")
    private Context context;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER, targetClass = Mode.class)
    @CollectionTable(name="questionnaire_mode", joinColumns = {@JoinColumn(name="questionnaire_id")})
    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private List<Mode> modes;

    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    BaseQuestionnaireEntity(String questionnaireId, CampaignEntity campaign, String label, Context context, List<Mode> modes) {
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
        questionnaire.setContext(getContext());
        questionnaire.setModes(getModes());
        questionnaire.setUpdatedDate(getUpdatedDate());
        return questionnaire;
    }

    public static BaseQuestionnaireEntity createFromModel(String campaignLabel, Questionnaire questionnaire) {
        CampaignEntity campaign = CampaignEntity.createWithLabel(campaignLabel);
        BaseQuestionnaireEntity questionnaireEntity = new BaseQuestionnaireEntity(questionnaire.getQuestionnaireId(), campaign, questionnaire.getLabel(),questionnaire.getContext(), questionnaire.getModes());
        Date date = Calendar.getInstance().getTime();
    
        questionnaireEntity.setCreationDate(date);
        questionnaireEntity.setUpdatedDate(date);
        return questionnaireEntity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((questionnaireId == null) ? 0 : questionnaireId.hashCode());
        result = prime * result + ((campaign == null) ? 0 : campaign.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + ((modes == null) ? 0 : modes.hashCode());
        result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
        result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseQuestionnaireEntity other = (BaseQuestionnaireEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (questionnaireId == null) {
            if (other.questionnaireId != null)
                return false;
        } else if (!questionnaireId.equals(other.questionnaireId))
            return false;
        if (campaign == null) {
            if (other.campaign != null)
                return false;
        } else if (!campaign.equals(other.campaign))
            return false;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (modes == null) {
            if (other.modes != null)
                return false;
        } else if (!modes.equals(other.modes))
            return false;
        if (creationDate == null) {
            if (other.creationDate != null)
                return false;
        } else if (!creationDate.equals(other.creationDate))
            return false;
        if (updatedDate == null) {
            if (other.updatedDate != null)
                return false;
        } else if (!updatedDate.equals(other.updatedDate))
            return false;
        return true;
    }

    
}
