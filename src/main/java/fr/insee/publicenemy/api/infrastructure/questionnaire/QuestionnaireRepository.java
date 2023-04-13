package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
@Slf4j
public class QuestionnaireRepository implements QuestionnairePort {

    private final QuestionnaireEntityRepository questionnaireEntityRepository;

    private final I18nMessagePort messageService;

    private static final String QUESTIONNAIRE_NOT_FOUND_KEY = "questionnaire.notfound";

    /**
     * Constructor
     *
     * @param questionnaireEntityRepository questionnaire repository
     */
    public QuestionnaireRepository(QuestionnaireEntityRepository questionnaireEntityRepository, I18nMessagePort messageService) {
        this.questionnaireEntityRepository = questionnaireEntityRepository;
        this.messageService = messageService;
    }

    @Override
    public List<Questionnaire> getQuestionnaires() {
        return questionnaireEntityRepository.findAll().stream().map(QuestionnaireEntity::toModel).toList();
    }

    @Override
    public Questionnaire getQuestionnaire(Long questionnaireId) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaireId)
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, Long.toString(questionnaireId))));
        return questionnaireEntity.toModel();
    }

    @Override
    public Questionnaire getQuestionnaire(String poguesId) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findByPoguesId(poguesId)
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, poguesId)));
        return questionnaireEntity.toModel();
    }

    @Override
    public boolean hasQuestionnaire(String poguesId) {
        return questionnaireEntityRepository.existsByPoguesId(poguesId);
    }

    @Override
    public byte[] getSurveyUnitData(Long questionnaireId) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaireId)
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, Long.toString(questionnaireId))));
        return questionnaireEntity.getSurveyUnitData();
    }

    @Override
    public Questionnaire addQuestionnaire(Questionnaire questionnaire) {
        QuestionnaireEntity questionnaireEntity = QuestionnaireEntity.createEntity(questionnaire);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        // add surveyUnitData to model as it is not retrieved in DB for perf reasons
        return questionnaireEntity.toModel(questionnaire.getSurveyUnitData());
    }

    @Override
    public Questionnaire updateQuestionnaire(Questionnaire questionnaire) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaire.getId())
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, Long.toString(questionnaire.getId()))));

        questionnaireEntity.update(questionnaire);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        return questionnaireEntity.toModel();
    }

    @Override
    public void deleteQuestionnaire(Long id) {
        questionnaireEntityRepository.deleteById(id);
    }

    @Override
    public Questionnaire updateQuestionnaireState(Questionnaire questionnaire) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaire.getId())
                .orElseThrow(() -> new RepositoryEntityNotFoundException(messageService.getMessage(QUESTIONNAIRE_NOT_FOUND_KEY, Long.toString(questionnaire.getId()))));

        questionnaireEntity.updateState(questionnaire);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        return questionnaireEntity.toModel();
    }
} 
