package fr.insee.publicenemy.api.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
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

import java.io.*;
import java.util.*;

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

        List<Interrogation> interrogations = new ArrayList<>();
        for (int id = 1; id <= interrogationsJsonLines.size(); id++) {
            InterrogationJsonLine interrogationJsonLine = interrogationsJsonLines.get(id - 1);
            interrogations.add(initInterrogation(interrogationJsonLine, questionnaireModelId));
        }
        return interrogations;
    }

    @Override
    public void updateInterrogationData(Questionnaire questionnaire, List<Interrogation> interrogations) {
        byte[] interrogationDataUpdated = addInterrogationIdToData(questionnaire.getInterrogationData(), interrogations);
        questionnaire.setInterrogationData(interrogationDataUpdated);
    }

    @Override
    public Interrogation getJsonInterrogation(String interrogationId, byte[] interrogationData) {
        List<InterrogationJsonLine> interrogationsJsonLines = getInterrogationsJsonLines(interrogationData);

        Optional<InterrogationJsonLine> interrogationsJsonLine = interrogationsJsonLines.stream()
                .filter(line -> {
                    if(line.getFields() != null && line.getFields().get("id") != null)
                        return interrogationId.equals(line.getFields().get("id").asText());
                    return false;
                })
                .findFirst();
        if(interrogationsJsonLine.isEmpty()) throw new InterrogationJsonNotFoundException(messageService.getMessage("interrogation.not-found", interrogationId));
        return initInterrogation(interrogationsJsonLine.get(), null);
    }

    /**
     * @param interrogationJsonLine    csv line containing a survey unit
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit from a line in the csv file
     */
    private Interrogation initInterrogation(@NonNull InterrogationJsonLine interrogationJsonLine, String questionnaireModelId) {
        InterrogationData interrogationData = new InterrogationData(interrogationJsonLine);

        String interrogationId = "";

        if(interrogationJsonLine.getFields() != null && interrogationJsonLine.getFields().get("id") != null){
            interrogationId = interrogationJsonLine.getFields().get("id").asText();
        }

        if(interrogationId.isEmpty()){
            interrogationId = UUID.randomUUID().toString();
        }

        return new Interrogation(interrogationId, questionnaireModelId, interrogationData, InterrogationStateData.createInitialStateData());
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

    public byte[] addInterrogationIdToData(byte[] jsonInput, List<Interrogation> interrogations){
        try {
            JsonNode root = objectMapper.readTree(jsonInput);
            if (root.isArray()) {
                for (int index = 0; index < root.size(); index++) {
                    JsonNode node = root.get(index);
                    JsonNode existingId = node.get("id");
                    String existingIdText = (existingId != null && !existingId.isNull() ) ? existingId.asText() : "";
                    if(existingId == null || existingId.isNull() || existingId.asText().isEmpty()) {
                        ((ObjectNode) node).put("id", existingIdText + "|" + interrogations.get(index).id());
                    }
                }
                return objectMapper.writeValueAsBytes(root);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }
}
