package fr.insee.publicenemy.api.application.ports;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;

import java.util.List;

public interface PersonalizationPort {

    List<PersonalizationMapping> getPersonalizationMappings();
    PersonalizationMapping getPersonalizationMapping(String interrogationId);
    PersonalizationMapping addPersonalizationMapping(PersonalizationMapping personalizationMapping);
    List<PersonalizationMapping> getPersonalizationMappingsByQuestionnaireId(Long questionnaireId);
    List<PersonalizationMapping> getPersonalizationMappingsByQuestionnaireIdAndMode(Long questionnaireId, Mode mode);
}
