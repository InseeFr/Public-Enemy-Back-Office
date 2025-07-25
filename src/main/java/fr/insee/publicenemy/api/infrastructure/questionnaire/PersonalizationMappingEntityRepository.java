package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.PersonalizationMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalizationMappingEntityRepository extends JpaRepository<PersonalizationMappingEntity, UUID> {

    Optional<List<PersonalizationMappingEntity>> findByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
    Optional<List<PersonalizationMappingEntity>> findByQuestionnaireId(Long questionnaireId);
    Long deleteByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
}
