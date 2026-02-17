package fr.insee.publicenemy.api.infrastructure.queen.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class QuestionnaireMetadataSerializer extends StdSerializer<QuestionnaireMetadataDto> {

    public QuestionnaireMetadataSerializer() {
        this(null);
    }

    public QuestionnaireMetadataSerializer(Class<QuestionnaireMetadataDto> t) {
        super(t);
    }

    @Override
    public void serialize(QuestionnaireMetadataDto metadata, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
        jgen.writeStartObject();
        jgen.writeStringProperty("inseeContext", metadata.inseeContext());
        jgen.writePOJOProperty("variables", metadata.metadataAttributes());
        jgen.writeEndObject();
    }
}