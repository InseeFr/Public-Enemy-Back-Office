package fr.insee.publicenemy.api.infrastructure.questionnaire;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntitySummary;
import lombok.extern.slf4j.Slf4j;

@Repository
@Transactional
@Slf4j
public class QuestionnaireRepository implements QuestionnairePort {

    private final CampaignEntityRepository campaignEntityRepository;
    private final QuestionnaireEntityRepository questionnaireEntityRepository;
    private final QuestionnaireEntitySummaryRepository questionnaireEntitySummaryRepository;

    /**
     * Constructor
     * @param campaignEntityRepository
     * @param questionnaireEntityRepository
     * @param questionnaireEntitySummaryRepository
     */
    public QuestionnaireRepository(CampaignEntityRepository campaignEntityRepository,
            QuestionnaireEntityRepository questionnaireEntityRepository, 
            QuestionnaireEntitySummaryRepository questionnaireEntitySummaryRepository) {
        this.campaignEntityRepository = campaignEntityRepository;
        this.questionnaireEntityRepository = questionnaireEntityRepository;
        this.questionnaireEntitySummaryRepository = questionnaireEntitySummaryRepository;
    }

    @Override
    public List<Questionnaire> getQuestionnaires() {
        return questionnaireEntitySummaryRepository.findAll().stream().map(QuestionnaireEntitySummary::toModel).toList();
    }

    @Override
    public Questionnaire getQuestionnaire(Long questionnaireId) {
        QuestionnaireEntitySummary questionnaireEntity = questionnaireEntitySummaryRepository.findById(questionnaireId)
                .orElseThrow(() -> new  RepositoryEntityNotFoundException("Questionnaire not found"));
        return questionnaireEntity.toModel();
    }

    @Override
    public Questionnaire addQuestionnaire(Questionnaire questionnaire) {
        long campaignCount = campaignEntityRepository.count();
        QuestionnaireEntity questionnaireEntity = QuestionnaireEntity.createFromModel("Campagne " + campaignCount, questionnaire);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        return questionnaireEntity.toModel();
    }

    @Override
    public Questionnaire updateQuestionnaire(Questionnaire questionnaire) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaire.getId())
                .orElseThrow(() -> new  RepositoryEntityNotFoundException("Questionnaire not found"));

        questionnaireEntity.update(questionnaire);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        log.error(questionnaireEntity.getContext().toString());
        return questionnaireEntity.toModel();
    }

    @Override
    public void deleteQuestionnaire(Long id) {
        questionnaireEntityRepository.deleteById(id);
    }
} 
