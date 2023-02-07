package fr.insee.publicenemy.api.controllers;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.SurveyUnit;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import fr.insee.publicenemy.api.application.usecase.QueenUseCase;
import fr.insee.publicenemy.api.controllers.dto.SurveyUnitsRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@Slf4j
public class SurveyUnitController {

    private final QueenUseCase queenUseCase;

    public SurveyUnitController(QueenUseCase queenUseCase) {
        this.queenUseCase = queenUseCase;
    }

    /**
     *
     * @param questionnaireId questionnaire id
     * @param modeName insee mode
     * @return all survey units fro the questionnaire
     */
    @GetMapping("/{questionnaireId}/modes/{modeName}/survey-units")
    public SurveyUnitsRest getSurveyUnits(@PathVariable Long questionnaireId, @PathVariable String modeName) {
        String questionnaireModelId = IdentifierGenerationUtils.generateQueenIdentifier(questionnaireId, Mode.valueOf(modeName));
        List<SurveyUnit> surveyUnits = queenUseCase.getSurveyUnits(questionnaireModelId);
        return SurveyUnitsRest.fromModel(surveyUnits, questionnaireModelId);
    }
}
