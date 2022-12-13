package fr.insee.publicenemy.api.infrastructure.questionnaire;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.CampaignEntity;

public interface CampaignEntityRepository extends JpaRepository<CampaignEntity, Long> {

} 
