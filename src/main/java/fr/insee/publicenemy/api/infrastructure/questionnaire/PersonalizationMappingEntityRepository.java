package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.PersonalizationMappingEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonalizationMappingEntityRepository extends JpaRepository<PersonalizationMappingEntity, UUID> {

    Optional<List<PersonalizationMappingEntity>> findByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
    Long deleteByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
}
