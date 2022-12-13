package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="questionnaire")
public class QuestionnaireEntitySummary extends BaseQuestionnaireEntity {
}
