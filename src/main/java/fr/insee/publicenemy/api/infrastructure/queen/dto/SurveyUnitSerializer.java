package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.ISurveyUnitDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.surveyunit.SurveyUnitData;

import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.Map;

public class SurveyUnitSerializer extends StdSerializer<SurveyUnitDto> {

    @Serial
    private static final long serialVersionUID = 5928430315100640987L;

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

        List<PersonalizationAttributeDto<String>> personalizationData = PersonalizationAttributeDto.getDefaultAttributes();

        jgen.writeStartObject();
        jgen.writeStringField("id", surveyUnit.id());
        jgen.writeStringField("questionnaireId", surveyUnit.questionnaireId());
        jgen.writeObjectField("personalization", personalizationData);
        jgen.writeObjectFieldStart("comment");
        jgen.writeEndObject();
        jgen.writeObjectFieldStart("data");
        jgen.writeObjectFieldStart("EXTERNAL");
        SurveyUnitData data = surveyUnit.data();
        if (data != null) {
            for (Map.Entry<String, ISurveyUnitDataAttributeValue<?>> attribute : data.getAttributes().entrySet()) {
                ISurveyUnitDataAttributeValue<?> objectData = attribute.getValue();
                jgen.writeObjectField(attribute.getKey(), objectData.getValue());
            }
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
        jgen.writeObjectField("stateData", surveyUnit.stateData());
        jgen.writeEndObject();
    }
}