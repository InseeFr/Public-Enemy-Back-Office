package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireModeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@DataJpaTest
class QuestionnaireEntityRepositoryTest {
    @Autowired
    private QuestionnaireEntityRepository repository;

    @Test
    void shouldSaveQuestionnaire() {
        List<QuestionnaireMode> questionnaireModes = QuestionnaireMode.toModel(List.of(Mode.CAWI, Mode.CAPI));
        QuestionnaireEntity questionnaire = new QuestionnaireEntity("l8wwljbo", "questionnaire label", Context.BUSINESS, questionnaireModes, "content".getBytes(), false);
        QuestionnaireEntity savedQuestionnaire = repository.saveAndFlush(questionnaire);
        assertThat(savedQuestionnaire).usingRecursiveComparison().ignoringFields("id").isEqualTo(questionnaire);
    }

    @Test
    void onFindAllReturnsAllQuestionnaires() {
        List<QuestionnaireEntity> questionnaires = repository.findAll();
        assertEquals(3, questionnaires.size());
    }

    @Test
    void onFindByIdReturnsCorrectQuestionnaire() {
        List<QuestionnaireMode> questionnaireModes = QuestionnaireMode.toModel(List.of(Mode.CAWI, Mode.CAPI));
        QuestionnaireEntity questionnaire = new QuestionnaireEntity("l8wwljbo", "questionnaire_label 1", Context.HOUSEHOLD, questionnaireModes, "content1".getBytes(), true);
        QuestionnaireEntity savedQuestionnaire = repository.findById(1L).get();
        assertEquals(1L, savedQuestionnaire.getId());
        List<QuestionnaireModeEntity> modes = savedQuestionnaire.getModeEntities();
        assertEquals(2, modes.size());
        assertEquals("CAWI", modes.get(0).getMode().name());
        assertEquals("OK", modes.get(0).getSynchronisationState());
        assertEquals("CAPI", modes.get(1).getMode().name());
        assertEquals("OK", modes.get(1).getSynchronisationState());
        assertEquals(savedQuestionnaire.getContext(), questionnaire.getContext());
        assertEquals(savedQuestionnaire.getPoguesId(), questionnaire.getPoguesId());
        assertEquals(savedQuestionnaire.isSynchronized(), questionnaire.isSynchronized());
        assertEquals(savedQuestionnaire.getLabel(), questionnaire.getLabel());
    }
}
