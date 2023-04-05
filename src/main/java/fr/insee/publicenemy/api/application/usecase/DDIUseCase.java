package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
import fr.insee.publicenemy.api.application.ports.DdiServicePort;
import fr.insee.publicenemy.api.application.ports.EnoServicePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DDIUseCase {

    private final DdiServicePort ddiService;
    private final EnoServicePort enoService;

    public DDIUseCase(DdiServicePort ddiService, EnoServicePort enoService) {
        this.ddiService = ddiService;
        this.enoService = enoService;
    }

    /**
     * Get questionnaire
     *
     * @param poguesId pogues questionnaire id
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(String poguesId) {
        return ddiService.getQuestionnaire(poguesId);
    }

    /**
     * Get DDI as XML format from questionnaire Id
     *
     * @param poguesId pogues questionnaire id
     * @return DDI
     */
    public Ddi getDdi(String poguesId) {
        return ddiService.getDdi(poguesId);
    }

    /**
     * Convert DDI with given identifier to a Lunatic questionnaire (json format)
     *
     * @param ddi     Ddi
     * @param context insee context
     * @param mode    questionnaire mode
     * @return Json Lunatic
     */
    public JsonLunatic getJsonLunatic(Ddi ddi, Context context, Mode mode) {
        return enoService.getJsonLunatic(ddi, context, mode);
    }

    /**
     * Get Json Pogues variables
     *
     * @param questionnaireId pogues questionnaire Id
     * @return variables type for a questionnaire. It only returns external variables right now
     */
    public List<VariableType> getQuestionnaireVariables(String questionnaireId) {
        List<VariableType> variables = ddiService.getQuestionnaireVariables(questionnaireId);
        // return only external variables types (may be extended later)
        return variables.stream().filter(variable -> variable.type() == VariableTypeEnum.EXTERNAL).toList();
    }
}
