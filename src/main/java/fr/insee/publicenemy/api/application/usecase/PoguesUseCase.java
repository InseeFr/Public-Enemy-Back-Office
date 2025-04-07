package fr.insee.publicenemy.api.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.publicenemy.api.application.domain.model.*;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
import fr.insee.publicenemy.api.application.ports.EnoServicePort;
import fr.insee.publicenemy.api.application.ports.PoguesServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@Slf4j
public class PoguesUseCase {

    private final PoguesServicePort poguesServicePort;
    private final EnoServicePort enoService;

    public PoguesUseCase(PoguesServicePort poguesServicePort, EnoServicePort enoService) {
        this.poguesServicePort = poguesServicePort;
        this.enoService = enoService;
    }

    /**
     * Get questionnaire
     *
     * @param poguesId pogues questionnaire id
     * @return the questionnaire
     */
    public Questionnaire getQuestionnaire(String poguesId) {
        return poguesServicePort.getQuestionnaire(poguesId);
    }

    /**
     * Get DDI as XML format from questionnaire Id
     *
     * @param poguesId pogues questionnaire id
     * @return DDI
     */
    public QuestionnaireModel getQuestionnaireModel(String poguesId) {
        log.info(poguesId + ": get pogues-model");
        return poguesServicePort.getQuestionnaireModel(poguesId);
    }

    /**
     * Convert DDI with given identifier to a Lunatic questionnaire (json format)
     *
     * @param questionnaireModel     QuestionnaireModel
     * @param context insee context
     * @param mode    questionnaire mode
     * @return Json Lunatic
     */
    public JsonLunatic getJsonLunatic(QuestionnaireModel questionnaireModel, Context context, Mode mode) {
        log.info(questionnaireModel.poguesId() + ": get JSON Lunatic for mode " + mode.name());
        return enoService.getJsonLunatic(questionnaireModel, context, mode);
    }

    /**
     * Get Json Pogues variables
     *
     * @param questionnaireId pogues questionnaire Id
     * @return variables type for a questionnaire. It only returns external variables right now
     */
    public List<VariableType> getQuestionnaireVariables(String questionnaireId) {
        List<VariableType> variables = poguesServicePort.getQuestionnaireVariables(questionnaireId);
        // return only external variables types (may be extended later)
        return variables.stream().filter(variable -> variable.type() == VariableTypeEnum.EXTERNAL).toList();
    }

    public JsonNode getNomenclatureOfQuestionnaire(String poguesId){
        log.info(poguesId + ": get nomenclatures");
        return poguesServicePort.getNomenclaturesByQuestionnaire(poguesId);
    }
}
