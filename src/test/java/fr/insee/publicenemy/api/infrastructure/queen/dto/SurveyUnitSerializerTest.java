package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.ISurveyUnitDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitDataAttributeValueList;
import fr.insee.publicenemy.api.infrastructure.csv.SurveyUnitStateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SurveyUnitSerializerTest {

    @Test
    void checkJsonFormatOnSerialize() throws JsonProcessingException {
        List<SurveyUnitDto> surveyUnits = new ArrayList<>();

        Map<String, ISurveyUnitDataAttributeValue<?>> attributes = new TreeMap<>();

        SurveyUnitDataAttributeValue booleanValue = new SurveyUnitDataAttributeValue("1");
        SurveyUnitDataAttributeValue textValue = new SurveyUnitDataAttributeValue("CS 70058");
        SurveyUnitDataAttributeValueList listValue = new SurveyUnitDataAttributeValueList();
        listValue.addValue("value1");
        listValue.addValue("value2");
        listValue.addValue("value3");
        listValue.addValue("value4");
        listValue.addValue("value5");
        listValue.addValue("value6");


        attributes.put("att1", booleanValue);
        attributes.put("att2", textValue);
        attributes.put("att3", listValue);

        SurveyUnitData data = new SurveyUnitData(attributes);

        surveyUnits.add(new SurveyUnitDto("1", "q1", data, SurveyUnitStateData.createInitialStateData()));
        surveyUnits.add(new SurveyUnitDto("2", "q2", data, SurveyUnitStateData.createInitialStateData()));

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(SurveyUnitDto.class, new SurveyUnitSerializer());
        mapper.registerModule(module);

        String jsonSurveyUnits = mapper.writeValueAsString(surveyUnits);

        assertEquals("""
                        [
                           {
                              "id":"1",
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
}
