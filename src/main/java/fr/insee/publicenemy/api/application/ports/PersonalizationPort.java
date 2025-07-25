package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;

import java.util.List;

public interface PersonalizationPort {

    PersonalizationMapping getPersonalizationMapping(String interrogationId);
    PersonalizationMapping addPersonalizationMapping(PersonalizationMapping personalizationMapping);
    List<PersonalizationMapping> getPersonalizationMappingsByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
    void deletePersonalizationMappingsByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
}
