package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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
@Entity
@Table(name="questionnaire")
public class QuestionnaireEntity extends BaseQuestionnaireEntity {

    @Lob
    @Column
    @NotNull
    private byte[] surveyUnitData;

    public QuestionnaireEntity(String questionnaireId, CampaignEntity campaign, String label, Context context, List<Mode> modes, byte[] surveyUnitData) {
        super(questionnaireId, campaign, label, context, modes);
        this.surveyUnitData = surveyUnitData;
    }

    public static QuestionnaireEntity createFromModel(String campaignLabel, Questionnaire questionnaire) {
        CampaignEntity campaign = CampaignEntity.createWithLabel(campaignLabel);
        QuestionnaireEntity questionnaireEntity = new QuestionnaireEntity(questionnaire.getQuestionnaireId(), campaign, questionnaire.getLabel(),questionnaire.getContext(), questionnaire.getModes(), questionnaire.getSurveyUnitData());
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
        setContext(questionnaire.getContext());        
        setUpdatedDate(Calendar.getInstance().getTime());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(surveyUnitData);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuestionnaireEntity other = (QuestionnaireEntity) obj;
        if (!Arrays.equals(surveyUnitData, other.surveyUnitData))
            return false;
        return true;
    }

    
}
