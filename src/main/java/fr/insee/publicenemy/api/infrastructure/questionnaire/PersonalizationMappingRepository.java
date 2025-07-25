package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.PersonalizationMappingEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
@Slf4j
public class PersonalizationMappingRepository {

    private final PersonalizationMappingEntityRepository mappingEntityRepository;

    private final I18nMessagePort messageService;

    private static final String QUESTIONNAIRE_NOT_FOUND_KEY = "questionnaire.notfound";

    /**
     * Constructor
     *
     * @param mappingEntityRepository  repository
     */
    public PersonalizationMappingRepository(PersonalizationMappingEntityRepository mappingEntityRepository, I18nMessagePort messageService) {
        this.mappingEntityRepository = mappingEntityRepository;
        this.messageService = messageService;
    }

    public List<PersonalizationMapping> getPersonalizationMappings() {
        return mappingEntityRepository.findAll().stream().map(PersonalizationMappingEntity::toModel).toList();
    }

    public PersonalizationMapping getPersonalizationMapping(String interrogationId) {
        PersonalizationMappingEntity mappingEntity = mappingEntityRepository.findById(UUID.fromString(interrogationId))
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, interrogationId)));
        return mappingEntity.toModel();
    }

    public PersonalizationMapping addPersonalizationMapping(PersonalizationMapping mapping) {
        PersonalizationMappingEntity entity = PersonalizationMappingEntity.createEntity(mapping);
        entity = mappingEntityRepository.save(entity);
        return entity.toModel();
    }
} 
