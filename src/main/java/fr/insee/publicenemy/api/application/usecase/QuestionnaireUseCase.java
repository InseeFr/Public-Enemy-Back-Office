package fr.insee.publicenemy.api.application.usecase;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.infrastructure.questionnaire.QuestionnaireRepository;

@Service
public class QuestionnaireUseCase {
    private final QuestionnaireRepository questionnairePort;
    private final DDIUseCase ddiUseCase;

    public QuestionnaireUseCase(QuestionnaireRepository questionnairePort, DDIUseCase ddiUseCase) {
        this.questionnairePort = questionnairePort;
        this.ddiUseCase = ddiUseCase;
    }

    /**
     * Add questionnaire
     * @param questionnaireId
     * @param contextId
     * @param csvContent
     * @return the saved questionnaire
     */
    public Questionnaire addQuestionnaire(String questionnaireId, Long contextId, byte[] csvContent) {
        Context context = questionnairePort.getContext(contextId);        
        Ddi ddi = ddiUseCase.getDdi(questionnaireId);
        List<String> modesString = ddi.getModes();

        List<Mode> modes = modesString.stream()
                .map(questionnairePort::getModeByName)
                .collect(Collectors.toList());  
        Questionnaire questionnaire = new Questionnaire(questionnaireId, ddi.getLabel(), context, modes, csvContent);
        return questionnairePort.addQuestionnaire(questionnaire);
    }
}
