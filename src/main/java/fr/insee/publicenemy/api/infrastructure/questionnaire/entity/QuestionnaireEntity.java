package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="questionnaire")
public class QuestionnaireEntity extends BaseQuestionnaireEntity {

    @Lob
    @Column
    @NotNull
    private byte[] surveyUnitData;

    public QuestionnaireEntity(String questionnaireId, CampaignEntity campaign, String label, ContextEntity context, List<ModeEntity> modes, byte[] surveyUnitData) {
        super(questionnaireId, campaign, label, context, modes);
        this.surveyUnitData = surveyUnitData;
    }

    public static QuestionnaireEntity createFromModel(String campaignLabel, Questionnaire questionnaire) {
        ContextEntity context = ContextEntity.createFromModel(questionnaire.getContext());
        List<ModeEntity> modesQuestionnaire = questionnaire.getModes().stream().map(ModeEntity::createFromModel).collect(Collectors.toList());
        CampaignEntity campaign = CampaignEntity.createWithLabel(campaignLabel);
        QuestionnaireEntity questionnaireEntity = new QuestionnaireEntity(questionnaire.getQuestionnaireId(), campaign, questionnaire.getLabel(),context, modesQuestionnaire, questionnaire.getSurveyUnitData());
        Date date = Calendar.getInstance().getTime();
    
        questionnaireEntity.setCreationDate(date);
        questionnaireEntity.setUpdatedDate(date);
        return questionnaireEntity;
    }

    public void update(Questionnaire questionnaire) {        
        byte [] questionnaireUnitData = questionnaire.getSurveyUnitData();
        if(questionnaireUnitData != null && questionnaireUnitData.length > 0) {
            setSurveyUnitData(questionnaireUnitData);
        }
        setContext(ContextEntity.createFromModel(questionnaire.getContext()));        
        setUpdatedDate(Calendar.getInstance().getTime());
    }
}
