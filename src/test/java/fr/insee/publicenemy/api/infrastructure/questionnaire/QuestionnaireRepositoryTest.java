package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireRepositoryTest {

    @Mock
    private QuestionnaireEntityRepository questionnaireEntityRepository;
    @Mock
    private I18nMessagePort messageService;

    @Mock
    private QuestionnaireEntity questionnaireEntity;

    private QuestionnaireRepository repository;

    private Questionnaire questionnaire;

    @BeforeEach
    public void init() {
        repository = new QuestionnaireRepository(questionnaireEntityRepository, messageService);
        questionnaire = new Questionnaire(1L, "l8wwljbo", "uuid", "label", Context.HOUSEHOLD, List.of(new QuestionnaireMode(Mode.CAWI)), "content".getBytes(), true, null, false);
    }

    @Test
    void onGetQuestionnaireWhenQuestionnaireNotExistsThrowRepositoryEntityNotFoundException() {
        Long questionnaireId = 1L;
        Optional<QuestionnaireEntity> emptyQuestionnaire = Optional.empty();
        when(questionnaireEntityRepository.findById(questionnaireId)).thenReturn(emptyQuestionnaire);
        assertThrows(RepositoryEntityNotFoundException.class, () -> repository.getQuestionnaire(questionnaireId));
    }

    @Test
    void onGetQuestionnaireFromPoguesIdWhenQuestionnaireNotExistsThrowRepositoryEntityNotFoundException() {
        String poguesId = "l8wwljbo";
        Optional<QuestionnaireEntity> emptyQuestionnaire = Optional.empty();
        when(questionnaireEntityRepository.findByPoguesId(poguesId)).thenReturn(emptyQuestionnaire);
        assertThrows(RepositoryEntityNotFoundException.class, () -> repository.getQuestionnaire(poguesId));
    }

    @Test
    void onGetQuestionnaireFromPoguesIdShouldCallRepository() {
        String poguesId = "l8wwljbo";
        when(questionnaireEntityRepository.findByPoguesId(poguesId)).thenReturn(Optional.of(questionnaireEntity));
        repository.getQuestionnaire(poguesId);
        verify(questionnaireEntityRepository).findByPoguesId(poguesId);
    }

    @Test
    void onUpdateQuestionnaireWhenQuestionnaireNotExistsThrowRepositoryEntityNotFoundException() {
        Optional<QuestionnaireEntity> emptyQuestionnaire = Optional.empty();
        when(questionnaireEntityRepository.findById(questionnaire.getId())).thenReturn(emptyQuestionnaire);
        assertThrows(RepositoryEntityNotFoundException.class, () -> repository.updateQuestionnaire(questionnaire));
    }

    @Test
    void onUpdateQuestionnaireShouldSaveQuestionnaire() {
        when(questionnaireEntityRepository.findById(questionnaire.getId())).thenReturn(Optional.of(questionnaireEntity));
        when(questionnaireEntity.toModel()).thenReturn(questionnaire);
        when(questionnaireEntityRepository.save(questionnaireEntity)).thenReturn(questionnaireEntity);
        repository.updateQuestionnaire(questionnaire);
        verify(questionnaireEntityRepository).save(questionnaireEntity);
    }

    @Test
    void onUpdateQuestionnaireStateWhenQuestionnaireNotExistsThrowRepositoryEntityNotFoundException() {
        Optional<QuestionnaireEntity> emptyQuestionnaire = Optional.empty();
        when(questionnaireEntityRepository.findById(questionnaire.getId())).thenReturn(emptyQuestionnaire);
        assertThrows(RepositoryEntityNotFoundException.class, () -> repository.updateQuestionnaireState(questionnaire));
    }

    @Test
    void onUpdateQuestionnaireStateShouldSaveQuestionnaireState() {
        when(questionnaireEntityRepository.findById(questionnaire.getId())).thenReturn(Optional.of(questionnaireEntity));
        when(questionnaireEntity.toModel()).thenReturn(questionnaire);
        when(questionnaireEntityRepository.save(questionnaireEntity)).thenReturn(questionnaireEntity);
        repository.updateQuestionnaireState(questionnaire);
        verify(questionnaireEntityRepository).save(questionnaireEntity);
    }

    @Test
    void onGetInterrogationDataWhenQuestionnaireNotExistsThrowRepositoryEntityNotFoundException() {
        Long questionnaireId = 1L;
        Optional<QuestionnaireEntity> emptyQuestionnaire = Optional.empty();
        when(questionnaireEntityRepository.findById(questionnaireId)).thenReturn(emptyQuestionnaire);
        assertThrows(RepositoryEntityNotFoundException.class, () -> repository.getInterrogationData(questionnaireId));
    }
}
