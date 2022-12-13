package fr.insee.publicenemy.api.infrastructure.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;

public interface QuestionnaireEntityRepository extends JpaRepository<QuestionnaireEntity, Long> {

} 
