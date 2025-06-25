package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;

import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.Map;

public class InterrogationSerializer extends StdSerializer<InterrogationDto> {

    @Serial
    private static final long serialVersionUID = 5928430315100640987L;

    public InterrogationSerializer() {
        this(null);
    }

    public InterrogationSerializer(Class<InterrogationDto> t) {
        super(t);
    }

    @Override
    public void serialize(
            InterrogationDto interrogationDto, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        List<PersonalizationAttributeDto<String>> personalizationData = PersonalizationAttributeDto.getDefaultAttributes();

        jgen.writeStartObject();
        jgen.writeStringField("id", interrogationDto.id());
        jgen.writeStringField("questionnaireId", interrogationDto.questionnaireId());
        jgen.writeObjectField("personalization", personalizationData);
        jgen.writeObjectFieldStart("comment");
        jgen.writeEndObject();
        jgen.writeObjectFieldStart("data");
        jgen.writeObjectFieldStart("EXTERNAL");
        InterrogationData data = interrogationDto.data();
        if (data != null) {
            for (Map.Entry<String, IInterrogationDataAttributeValue<?>> attribute : data.getAttributes().entrySet()) {
                IInterrogationDataAttributeValue<?> objectData = attribute.getValue();
                jgen.writeObjectField(attribute.getKey(), objectData.getValue());
            }
        }
        jgen.writeEndObject();
        jgen.writeEndObject();
        jgen.writeObjectField("stateData", interrogationDto.stateData());
        jgen.writeEndObject();
    }
}