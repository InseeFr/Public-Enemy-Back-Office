package fr.insee.publicenemy.api.infrastructure.questionnaire;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.QuestionnairePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.CampaignEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ContextEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ModeEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;

@Repository
@Transactional
public class QuestionnaireRepository implements QuestionnairePort {

    private final CampaignEntityRepository campaignEntityRepository;
    private final QuestionnaireEntityRepository questionnaireEntityRepository;
    private final ContextEntityRepository contextEntityRepository;
    private final ModeEntityRepository modeEntityRepository;
    private final ModelMapper mapper;

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
            ContextEntityRepository contextEntityRepository, ModeEntityRepository modeEntityRepository, ModelMapper mapper) {
        this.campaignEntityRepository = campaignEntityRepository;
        this.questionnaireEntityRepository = questionnaireEntityRepository;
        this.contextEntityRepository = contextEntityRepository;
        this.modeEntityRepository = modeEntityRepository;
        this.mapper = mapper;
    }

    @Override
    public Questionnaire addQuestionnaire(Questionnaire questionnaire) {
        long campaignCount = campaignEntityRepository.count();
        CampaignEntity campaignEntity = new CampaignEntity("Campagne " + campaignCount);
        QuestionnaireEntity questionnaireEntity = mapper.map(questionnaire, QuestionnaireEntity.class);
        Date date = Calendar.getInstance().getTime();
        
        questionnaireEntity.setCampaign(campaignEntity);        
        questionnaireEntity.setCreationDate(date);
        questionnaireEntity.setUpdatedDate(date);
        questionnaireEntity = questionnaireEntityRepository.save(questionnaireEntity);
        return mapper.map(questionnaireEntity, Questionnaire.class);
    }

    @Override
    public Questionnaire updateQuestionnaire(Questionnaire questionnaire) {
        QuestionnaireEntity questionnaireEntity = questionnaireEntityRepository.findById(questionnaire.getId())
                .orElseThrow(() -> new  RepositoryEntityNotFoundException("Questionnaire not found"));
        QuestionnaireEntity mappedQuestionnaire = mapper.map(questionnaire, QuestionnaireEntity.class);

        mappedQuestionnaire.setCampaign(questionnaireEntity.getCampaign());
        mappedQuestionnaire.setUpdatedDate(Calendar.getInstance().getTime());
        questionnaireEntity = questionnaireEntityRepository.save(mappedQuestionnaire);
        return mapper.map(questionnaireEntity, Questionnaire.class);
    }

    @Override
    public List<Mode> getModes() {
        List<ModeEntity> modesEntity = modeEntityRepository.findAll();
        return mapList(modesEntity, Mode.class);
    }

    @Override
    public List<Context> getContexts() {
        List<ContextEntity> contextsEntity = contextEntityRepository.findAll();
        return mapList(contextsEntity, Context.class);
    }

    @Override
    public Context getContext(Long id) {
        ContextEntity contextEntity = contextEntityRepository.findById(id).orElseThrow(() -> new  RepositoryEntityNotFoundException("Context not found"));
        return mapper.map(contextEntity, Context.class);
    }

    @Override
    public Mode getMode(Long id) {
        ModeEntity modeEntity = modeEntityRepository.findById(id).orElseThrow(() -> new  RepositoryEntityNotFoundException("Mode not found"));
        return mapper.map(modeEntity, Mode.class);
    }

    
    @Override
    public Mode getModeByName(String name) {
        ModeEntity modeEntity = modeEntityRepository.findByName(name).orElseThrow(() -> new  RepositoryEntityNotFoundException("Mode not found"));
        return mapper.map(modeEntity, Mode.class);
    }

    /**
     * 
     * @param <S>
     * @param <T>
     * @param source
     * @param targetClass
     * @return the mapping of source to List<TargetClass>
     */
    private <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
          .stream()
          .map(element -> mapper.map(element, targetClass))
          .collect(Collectors.toList());
    }

} 
