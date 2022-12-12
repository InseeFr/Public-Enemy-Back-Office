package fr.insee.publicenemy.api.infrastructure.questionnaire;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ContextEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ModeEntity;
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
    private final ContextEntityRepository contextEntityRepository;
    private final ModeEntityRepository modeEntityRepository;

    /**
     * Constructor
     * @param campaignEntityRepository
     * @param questionnaireEntityRepository
     * @param contextEntityRepository
     * @param modeEntityRepository
     * @param mapper
     */
    public QuestionnaireRepository(CampaignEntityRepository campaignEntityRepository,
            QuestionnaireEntityRepository questionnaireEntityRepository, 
            QuestionnaireEntitySummaryRepository questionnaireEntitySummaryRepository,
            ContextEntityRepository contextEntityRepository, ModeEntityRepository modeEntityRepository) {
        this.campaignEntityRepository = campaignEntityRepository;
        this.questionnaireEntityRepository = questionnaireEntityRepository;
        this.questionnaireEntitySummaryRepository = questionnaireEntitySummaryRepository;
        this.contextEntityRepository = contextEntityRepository;
        this.modeEntityRepository = modeEntityRepository;
    }

    @Override
    public List<Questionnaire> getQuestionnaires() {
        return questionnaireEntitySummaryRepository.findAll().stream().map(QuestionnaireEntitySummary::toModel).collect(Collectors.toList());
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

    @Override
    public List<Mode> getModes() {
        return modeEntityRepository.findAll().stream().map(ModeEntity::toModel).collect(Collectors.toList());
    }

    @Override
    public List<Context> getContexts() {
        return contextEntityRepository.findAll().stream().map(ContextEntity::toModel).collect(Collectors.toList());
    }

    @Override
    public Context getContext(Long id) {
        ContextEntity contextEntity = contextEntityRepository.findById(id).orElseThrow(() -> new  RepositoryEntityNotFoundException("Context not found"));
        return contextEntity.toModel();
    }

    @Override
    public Mode getMode(Long id) {
        ModeEntity modeEntity = modeEntityRepository.findById(id).orElseThrow(() -> new  RepositoryEntityNotFoundException("Mode not found"));
        return modeEntity.toModel();
    }

    
    @Override
    public Mode getModeByName(String name) {
        ModeEntity modeEntity = modeEntityRepository.findByName(name).orElseThrow(() -> new  RepositoryEntityNotFoundException("Mode not found"));
        return modeEntity.toModel();
    }
} 
