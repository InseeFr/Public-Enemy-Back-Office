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

public class InterrogationUpdateSerializer extends StdSerializer<InterrogationUpdateDto> {

    @Serial
    private static final long serialVersionUID = 5928430315100640987L;


    private static final String COLLECTED = "COLLECTED";
    private static final String EXTERNAL = "EXTERNAL";

    public InterrogationUpdateSerializer() {
        this(null);
    }

    public InterrogationUpdateSerializer(Class<InterrogationUpdateDto> t) {
        super(t);
    }

    @Override
    public void serialize(
            InterrogationUpdateDto interrogation, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        List<PersonalizationAttributeDto<String>> personalizationData = PersonalizationAttributeDto.getDefaultAttributes();

        jgen.writeStartObject();
        jgen.writeObjectField("personalization", personalizationData);
        jgen.writeObjectFieldStart("comment");
        jgen.writeEndObject();
        jgen.writeObjectFieldStart("data");
        InterrogationData data = interrogation.data();
        if (data != null) {
            if(data.getExternalAttributes() != null) {
                jgen.writeObjectFieldStart(EXTERNAL);
                // External data
                for (Map.Entry<String, IInterrogationDataAttributeValue<?>> attribute : data.getExternalAttributes().entrySet()) {
                    IInterrogationDataAttributeValue<?> objectData = attribute.getValue();
                    jgen.writeObjectField(attribute.getKey(), objectData.getValue());
                }
                jgen.writeEndObject(); // close EXTERNAL
            }

            if(data.getCollectedAttributes() != null){
                jgen.writeObjectFieldStart(COLLECTED);
                // External data
                for (Map.Entry<String, IInterrogationDataAttributeValue<?>> attribute : data.getCollectedAttributes().entrySet()) {
                    IInterrogationDataAttributeValue<?> objectData = attribute.getValue();
                    jgen.writeObjectFieldStart(attribute.getKey());
                    jgen.writeObjectField(COLLECTED, objectData.getValue());
                    jgen.writeEndObject(); // close name of variable
                }
                jgen.writeEndObject(); // close COLLECTED
            }
        }
        jgen.writeEndObject(); // close data
        jgen.writeObjectField("stateData", interrogation.stateData());
        jgen.writeEndObject();
    }
}