package fr.insee.publicenemy.api.infrastructure.queen.dto;

import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationDataAttributeValueList;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class InterrogationUpdateSerializerTest {

    private static final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void checkJsonFormatOnSerialize() {
        List<InterrogationUpdateDto> surveyUnits = new ArrayList<>();

        Map<String, IInterrogationDataAttributeValue> attributes = new TreeMap<>();

        InterrogationDataAttributeValue<String> booleanValue = new InterrogationDataAttributeValue<>("1");
        InterrogationDataAttributeValue<String> textValue = new InterrogationDataAttributeValue<>("CS 70058");
        InterrogationDataAttributeValueList<String> listValue = new InterrogationDataAttributeValueList<>();
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

        surveyUnits.add(new InterrogationUpdateDto(data, InterrogationStateData.createInitialStateData()));
        surveyUnits.add(new InterrogationUpdateDto(data, InterrogationStateData.createInitialStateData()));

        String jsonSurveyUnits = objectMapper.writeValueAsString(surveyUnits);

        assertEquals("""
                        [
                           {
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
                                 },
                                 "COLLECTED":{}
                              },
                              "stateData": null
                           },
                           {
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
                                 },
                                 "COLLECTED":{}
                              },
                              "stateData": null
                           }
                        ]""".replaceAll("\\s+", "")
                , jsonSurveyUnits.replaceAll("\\s+", ""));
    }
}
