package fr.insee.publicenemy.api.infrastructure.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntitySummary;

public interface QuestionnaireEntitySummaryRepository  extends JpaRepository<QuestionnaireEntitySummary, Long> {
    
} 
    
