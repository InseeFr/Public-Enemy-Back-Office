package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SurveyUnitSerializer extends StdSerializer<SurveyUnitDto> {

    public SurveyUnitSerializer() {
        this(null);
    }

    public SurveyUnitSerializer(Class<SurveyUnitDto> t) {
        super(t);
    }

    @Override
    public void serialize(
            SurveyUnitDto surveyUnit, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        List<PersonalizationAttributeDto<String>> personalizationData = new ArrayList<>();
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers1", "Mr Dupond"));
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers2", ""));
        personalizationData.add(new PersonalizationAttributeDto<>("whoAnswers3", ""));

        jgen.writeStartObject();
            jgen.writeStringField("id", surveyUnit.id());
            jgen.writeStringField("questionnaireId", surveyUnit.questionnaireId());
            jgen.writeObjectField("personalization", personalizationData);
            jgen.writeObjectFieldStart("comment");
            jgen.writeEndObject();
            jgen.writeObjectFieldStart("data");
                jgen.writeObjectField("EXTERNAL", surveyUnit.data().getAttributes());
            jgen.writeEndObject();
            jgen.writeObjectField("stateData", surveyUnit.stateData());
        jgen.writeEndObject();
    }
}