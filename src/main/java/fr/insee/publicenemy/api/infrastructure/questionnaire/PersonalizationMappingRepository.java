package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.PersonalizationPort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.PersonalizationMappingEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional
@Slf4j
public class PersonalizationMappingRepository implements PersonalizationPort {

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

    @Override
    public PersonalizationMapping getPersonalizationMapping(String interrogationId) {
        PersonalizationMappingEntity mappingEntity = mappingEntityRepository.findById(UUID.fromString(interrogationId))
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, interrogationId)));
        return mappingEntity.toModel();
    }

    @Override
    public PersonalizationMapping addPersonalizationMapping(PersonalizationMapping mapping) {
        PersonalizationMappingEntity entity = PersonalizationMappingEntity.createEntity(mapping);
        entity = mappingEntityRepository.save(entity);
        return entity.toModel();
    }

    @Override
    public List<PersonalizationMapping> getPersonalizationMappingsByQuestionnaireIdAndMode(Long questionnaireId, Mode mode) {
        List<PersonalizationMappingEntity> mappingEntities =  mappingEntityRepository.findByQuestionnaireIdAndMode(questionnaireId, mode)
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, questionnaireId.toString())));
        return mappingEntities.stream().map(PersonalizationMappingEntity::toModel).toList();
    }

    @Override
    public void deletePersonalizationMappingsByQuestionnaireIdAndMode(Long questionnaireId, Mode mode) {
        Long nbDeleted = mappingEntityRepository.deleteByQuestionnaireIdAndMode(questionnaireId, mode);
        log.info("Perso deleted for questionnaireId {}, and mode {} : {}", questionnaireId, mode, nbDeleted);
    }
} 
