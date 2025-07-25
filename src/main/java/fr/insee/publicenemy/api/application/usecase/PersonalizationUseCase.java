package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.ports.PersonalizationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PersonalizationUseCase {

    private final PersonalizationPort personalizationPort;

    public PersonalizationUseCase(PersonalizationPort personalizationPort){
        this.personalizationPort = personalizationPort;
    }

    public List<PersonalizationMapping> getPersonalizationByQuestionnaireIdAndMode(Long questionnaireId, Mode mode){
        return personalizationPort.getPersonalizationMappingsByQuestionnaireIdAndMode(questionnaireId, mode);
    }

    public List<PersonalizationMapping> getPersonalizationByQuestionnaireId(Long questionnaireId){
        return personalizationPort.getPersonalizationMappingsByQuestionnaire(questionnaireId);
    }

    public PersonalizationMapping getPersoMappingByInterrogationId(String interrogationId){
        return personalizationPort.getPersonalizationMapping(interrogationId);
    }
}
