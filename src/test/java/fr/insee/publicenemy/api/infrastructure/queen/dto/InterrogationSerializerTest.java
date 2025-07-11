package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValueList;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import fr.insee.publicenemy.api.infrastructure.json.InterrogationJsonLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InterrogationSerializerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static JsonNode readJsonFromFile(File file) throws IOException {
        return objectMapper.readTree(file);
    }

    @Test
    void checkJsonFormatOnSerialize() throws JsonProcessingException {
        List<InterrogationDto> surveyUnits = new ArrayList<>();

        Map<String, IInterrogationDataAttributeValue> attributes = new TreeMap<>();

        InterrogationDataAttributeValue<String> booleanValue = new InterrogationDataAttributeValue<>("1");
        InterrogationDataAttributeValue<String>  textValue = new InterrogationDataAttributeValue<>("CS 70058");
        InterrogationDataAttributeValueList<String>  listValue = new InterrogationDataAttributeValueList<>();
        listValue.addValue("value1");
        listValue.addValue("value2");
        listValue.addValue("value3");
        listValue.addValue("value4");
        listValue.addValue("value5");
        listValue.addValue("value6");


        attributes.put("att1", booleanValue);
        attributes.put("att2", textValue);
        attributes.put("att3", listValue);

        InterrogationData data = new InterrogationData(attributes);

        surveyUnits.add(new InterrogationDto("1", "su-1","q1", data, InterrogationStateData.createInitialStateData()));
        surveyUnits.add(new InterrogationDto("2", "su-2","q2", data, InterrogationStateData.createInitialStateData()));

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(InterrogationDto.class, new InterrogationSerializer());
        mapper.registerModule(module);

        String jsonSurveyUnits = mapper.writeValueAsString(surveyUnits);

        assertEquals("""
                        [
                           {
                              "id":"1",
                              "surveyUnitId": "su-1",
                              "questionnaireId":"q1",
                              "personalization":[],
                              "comment":{
                              },
                              "data":{
                                 "EXTERNAL":{
                                    "att1":"1",
                                    "att2":"CS70058",
                                    "att3":[
                                       "value1",
                                       "value2",
                                       "value3",
                                       "value4",
                                       "value5",
                                       "value6"
                                    ]
                                 }
                              },
                              "stateData": null
                           },
                           {
                              "id":"2",
                              "surveyUnitId": "su-2",
                              "questionnaireId":"q2",
                              "personalization":[],
                              "comment":{
                              },
                              "data":{
                                 "EXTERNAL":{
                                    "att1":"1",
                                    "att2":"CS70058",
                                    "att3":[
                                       "value1",
                                       "value2",
                                       "value3",
                                       "value4",
                                       "value5",
                                       "value6"
                                    ]
                                 }
                              },
                              "stateData": null
                           }
                        ]""".replaceAll("\\s+", "")
                , jsonSurveyUnits.replaceAll("\\s+", ""));
    }

    @Test
    void checkJsonFormatOnSerialize_fromJson() throws IOException {
        List<InterrogationDto> surveyUnits = new ArrayList<>();

        String resourcePath = "src/test/resources/interrogation-data.json";
        File file = new File(resourcePath);



        InterrogationJsonLine interrogationJsonLine = new InterrogationJsonLine(readJsonFromFile(file));

        InterrogationData data = new InterrogationData(interrogationJsonLine);

        surveyUnits.add(new InterrogationDto("1", "su-1","q1", data, InterrogationStateData.createInitialStateData()));
        surveyUnits.add(new InterrogationDto("2", "su-2","q2", data, InterrogationStateData.createInitialStateData()));

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(InterrogationDto.class, new InterrogationSerializer());
        mapper.registerModule(module);

        String jsonSurveyUnits = mapper.writeValueAsString(surveyUnits);
        assertEquals("""
                        [
                           {
                             "id": "1",
                             "surveyUnitId": "su-1",
                             "questionnaireId": "q1",
                             "personalization": [],
                             "comment": {},
                             "data": {
                               "EXTERNAL": {
                                 "TEST_ARRAY": [
                                   "YOUHOU 1",
                                   "YOUHOU 2"
                                 ],
                                 "TEST": "YOUHOU !!"
                               },
                               "COLLECTED": {
                                 "TABLEAU_CODE21": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE11": {
                                   "COLLECTED": null
                                 },
                                 "QUESTIONNOMBRE": {
                                   "COLLECTED": 39
                                 },
                                 "TABLEAUDYN_MIN_MAX_VTL1": {
                                   "COLLECTED": []
                                 },
                                 "PRENOM": {
                                   "COLLECTED": [
                                     "Alice",
                                     "Bob"
                                   ]
                                 },
                                 "TABLEAUDYN_FIXED_SIZE1": {
                                   "COLLECTED": []
                                 },
                                 "TABLEAU_CODE_COLONNES11": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE_COLONNES22": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE_COLONNES12": {
                                   "COLLECTED": [
                                     [
                                       1,
                                       2
                                     ],
                                     [
                                       3,
                                       null
                                     ]
                                   ]
                                 },
                                 "TABLEAU_CODE_COLONNES21": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D11": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D22": {
                                   "COLLECTED": null
                                 },
                                 "QUESTIONTEXT": {
                                   "COLLECTED": "Bonjour"
                                 },
                                 "TABLEAU2D12": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D21": {
                                   "COLLECTED": null
                                 }
                               }
                             },
                             "stateData": null
                           },
                           {
                             "id": "2",
                             "surveyUnitId": "su-2",
                             "questionnaireId": "q2",
                             "personalization": [],
                             "comment": {},
                             "data": {
                               "EXTERNAL": {
                                 "TEST_ARRAY": [
                                   "YOUHOU 1",
                                   "YOUHOU 2"
                                 ],
                                 "TEST": "YOUHOU !!"
                               },
                               "COLLECTED": {
                                 "TABLEAU_CODE21": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE11": {
                                   "COLLECTED": null
                                 },
                                 "QUESTIONNOMBRE": {
                                   "COLLECTED": 39
                                 },
                                 "TABLEAUDYN_MIN_MAX_VTL1": {
                                   "COLLECTED": []
                                 },
                                 "PRENOM": {
                                   "COLLECTED": [
                                     "Alice",
                                     "Bob"
                                   ]
                                 },
                                 "TABLEAUDYN_FIXED_SIZE1": {
                                   "COLLECTED": []
                                 },
                                 "TABLEAU_CODE_COLONNES11": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE_COLONNES22": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU_CODE_COLONNES12": {
                                   "COLLECTED": [
                                     [
                                       1,
                                       2
                                     ],
                                     [
                                       3,
                                       null
                                     ]
                                   ]
                                 },
                                 "TABLEAU_CODE_COLONNES21": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D11": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D22": {
                                   "COLLECTED": null
                                 },
                                 "QUESTIONTEXT": {
                                   "COLLECTED": "Bonjour"
                                 },
                                 "TABLEAU2D12": {
                                   "COLLECTED": null
                                 },
                                 "TABLEAU2D21": {
                                   "COLLECTED": null
                                 }
                               }
                             },
                             "stateData": null
                           }
                         ]""".replaceAll("\\s+", "")
                , jsonSurveyUnits.replaceAll("\\s+", ""));
    }
}
