package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireMode;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.dto.QuestionnaireRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QuestionnaireComponentTest {
    @Mock
    private I18nMessagePort messagePort;
    
    private QuestionnaireComponent component;
    
    @BeforeEach
    void init() {
        component = new QuestionnaireComponent(messagePort);
    }
    
    @Test
    void onCreateFromModelReturnsAllAttributesFromQuestionnaire() {
        Questionnaire questionnaire = new Questionnaire(1L, "l8wwljbo", "label", Context.HOUSEHOLD, 
                List.of(new QuestionnaireMode(Mode.CAWI), new QuestionnaireMode(Mode.CAPI)), "content".getBytes(), true);
        
        QuestionnaireRest questionnaireRest = component.createFromModel(questionnaire);

        assertEquals(questionnaireRest.id(), questionnaire.getId());
        assertEquals(questionnaireRest.context().name(), questionnaire.getContext().name());
        assertEquals(questionnaireRest.poguesId(), questionnaire.getPoguesId());
        assertEquals(questionnaireRest.isSynchronized(), questionnaire.isSynchronized());
        assertEquals(questionnaireRest.label(), questionnaire.getLabel());
        assertEquals(questionnaireRest.modes().size(), questionnaire.getQuestionnaireModes().size());
    }
}
