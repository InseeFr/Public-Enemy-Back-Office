package fr.insee.publicenemy.api.infrastructure.queen.dto;

import fr.insee.publicenemy.api.application.domain.model.interrogation.IInterrogationDataAttributeValue;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.List;
import java.util.Map;

public class InterrogationSerializer extends StdSerializer<InterrogationDto> {

    private static final String COLLECTED = "COLLECTED";
    private static final String EXTERNAL = "EXTERNAL";

    public InterrogationSerializer() {
        this(null);
    }

    public InterrogationSerializer(Class<InterrogationDto> t) {
        super(t);
    }

    @Override
    public void serialize(InterrogationDto interrogationDto, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
        List<PersonalizationAttributeDto<String>> personalizationData = PersonalizationAttributeDto.getDefaultAttributes();

        jgen.writeStartObject();
        jgen.writeStringProperty("id", interrogationDto.id());
        jgen.writeStringProperty("surveyUnitId", interrogationDto.surveyUnitId());
        jgen.writeStringProperty("questionnaireId", interrogationDto.questionnaireId());
        jgen.writePOJOProperty("personalization", personalizationData);
        jgen.writePOJOProperty("comment", new Object());
        jgen.writeObjectPropertyStart("data");
        InterrogationData data = interrogationDto.data();
        if (data != null) {
            if(data.getExternalAttributes() != null) {
                jgen.writeObjectPropertyStart(EXTERNAL);
                // External data
                for (Map.Entry<String, IInterrogationDataAttributeValue> attribute : data.getExternalAttributes().entrySet()) {
                    IInterrogationDataAttributeValue objectData = attribute.getValue();
                    jgen.writePOJOProperty(attribute.getKey(), objectData.getValue());
                }
                jgen.writeEndObject(); // close EXTERNAL
            }

            if(data.getCollectedAttributes() != null){
                jgen.writeObjectPropertyStart(COLLECTED);
                // Collected data
                for (Map.Entry<String, IInterrogationDataAttributeValue> attribute : data.getCollectedAttributes().entrySet()) {
                    IInterrogationDataAttributeValue objectData = attribute.getValue();
                    jgen.writeObjectPropertyStart(attribute.getKey());
                    jgen.writePOJOProperty(COLLECTED, objectData.getValue());
                    jgen.writeEndObject(); // close name of variable
                }
                jgen.writeEndObject(); // close COLLECTED
            }
        }
        jgen.writeEndObject(); // close data
        jgen.writePOJOProperty("stateData", interrogationDto.stateData());
        jgen.writeEndObject();
    }
}