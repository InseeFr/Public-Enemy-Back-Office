package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.InterrogationJsonPort;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import fr.insee.publicenemy.api.infrastructure.json.exceptions.InterrogationJsonNotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InterrogationJsonService implements InterrogationJsonPort {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final I18nMessagePort messageService;

    public InterrogationJsonService(I18nMessagePort messagePort) {
        this.messageService = messagePort;
    }

    @Override
    public List<Interrogation> initInterrogations(byte[] interrogationData, String questionnaireModelId) {
        List<InterrogationJsonLine> interrogationsJsonLines = getInterrogationsJsonLines(interrogationData);
        return interrogationsJsonLines.stream()
                .map(line -> initInterrogation(line, null, questionnaireModelId))
                .toList();
    }

    @Override
    public Interrogation getJsonInterrogation(PersonalizationMapping persoMapping, byte[] interrogationData) {
        List<InterrogationJsonLine> interrogationsJsonLines = getInterrogationsJsonLines(interrogationData);

        if(interrogationsJsonLines.isEmpty() || persoMapping.dataIndex() >= interrogationsJsonLines.size() || persoMapping.dataIndex() < 0) throw new InterrogationJsonNotFoundException(messageService.getMessage("interrogation.not-found", persoMapping.interrogationId()));
        return initInterrogation(interrogationsJsonLines.get(persoMapping.dataIndex()), persoMapping.interrogationId(), persoMapping.getQuestionnaireModelId());
    }

    /**
     * @param interrogationJsonLine    csv line containing a survey unit
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit from a line in the csv file
     */
    private Interrogation initInterrogation(@NonNull InterrogationJsonLine interrogationJsonLine, String interrogationId, String questionnaireModelId) {
        InterrogationData interrogationData = new InterrogationData(interrogationJsonLine);
        String interroId = interrogationId != null ? interrogationId : UUID.randomUUID().toString();
        return new Interrogation(interroId, questionnaireModelId, interrogationData, InterrogationStateData.createInitialStateData());
    }



    /**
     * get a list of interrogations from interrogation csv data
     *
     * @param interrogationData interrogations csv data
     * @return list of interrogations from interrogation csv data
     */
    private List<InterrogationJsonLine> getInterrogationsJsonLines(byte[] interrogationData) {
        List<InterrogationJsonLine> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(interrogationData);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    InterrogationJsonLine jsonLine = new InterrogationJsonLine(node);
                    result.add(jsonLine);
                }
            } else {
                throw new ServiceException(
                        HttpStatus.NOT_ACCEPTABLE,
                        messageService.getMessage("validation.json.not.array.error"));
            }
        } catch (IOException e) {
            throw new ServiceException(
                    HttpStatus.NOT_ACCEPTABLE,
                    messageService.getMessage("validation.json.malform.error"));
        }
        return result;
    }
}
