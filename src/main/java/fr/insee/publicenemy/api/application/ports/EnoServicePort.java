package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.domain.model.JsonLunatic;
import fr.insee.publicenemy.api.application.domain.model.Mode;

public interface EnoServicePort {
    /**
     * Retrieve content as JSON Lunatic format from ENO 
     * @param ddi
     * @param context
     * @param mode
     * @return Json Lunatic
     */
    JsonLunatic getJsonLunatic(Ddi ddi, Context context, Mode mode);
}
