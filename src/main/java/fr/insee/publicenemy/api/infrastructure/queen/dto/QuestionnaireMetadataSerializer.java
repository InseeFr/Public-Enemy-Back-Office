package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class QuestionnaireMetadataSerializer extends StdSerializer<QuestionnaireMetadataDto> {

    public QuestionnaireMetadataSerializer() {
        this(null);
    }

    public QuestionnaireMetadataSerializer(Class<QuestionnaireMetadataDto> t) {
        super(t);
    }

    @Override
    public void serialize(
            QuestionnaireMetadataDto metadata, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();
            jgen.writeObjectFieldStart("value");
            jgen.writeStringField("inseeContext", metadata.inseeContext());
            jgen.writeObjectField("variables", metadata.metadataAttributes());
        jgen.writeEndObject();
    }
}