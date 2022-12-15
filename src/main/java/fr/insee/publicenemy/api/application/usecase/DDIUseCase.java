package fr.insee.publicenemy.api.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.ports.DdiServicePort;
import fr.insee.publicenemy.api.application.ports.EnoServicePort;

@Service
@Transactional
public class DDIUseCase {
    
    private DdiServicePort ddiService;
    private EnoServicePort enoService;

    public DDIUseCase(DdiServicePort ddiService, EnoServicePort enoService) {
        this.ddiService = ddiService;
        this.enoService = enoService;
    }

    /**
     * Get DDI as XML format from questionnaire Id
     * @param questionnaireId
     * @return DDI
     */
    public Ddi getDdi(String questionnaireId) {
        return ddiService.getDdi(questionnaireId);
    }

    /**
     * Get DDI as JSON Lunatic format
     * @param questionnaireId
     * @param context
     * @param mode
     * @return Json Lunatic
     */
    public JsonLunatic getJsonLunatic(String questionnaireId, Context context, Mode mode) {
        Ddi ddi = getDdi(questionnaireId);
        return enoService.getJsonLunatic(ddi, context, mode);
    }
}
