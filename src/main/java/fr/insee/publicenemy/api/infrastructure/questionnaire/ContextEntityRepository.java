package fr.insee.publicenemy.api.infrastructure.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ContextEntity;


public interface ContextEntityRepository extends JpaRepository<ContextEntity, Long> {
    
} 
