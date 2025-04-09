package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.QuestionnaireModel;

public interface EnoServicePort {
    /**
     * Retrieve content as JSON Lunatic format from ENO 
     * @param questionnaireModel questionnaireModel content
     * @param context insee context
     * @param mode questionnaire mode
     * @return Json Lunatic
     */
    JsonLunatic getJsonLunatic(QuestionnaireModel questionnaireModel, Context context, Mode mode) ;
}
