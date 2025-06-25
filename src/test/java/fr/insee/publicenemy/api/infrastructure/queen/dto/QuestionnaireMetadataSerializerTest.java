package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QuestionnaireMetadataSerializerTest {

    @Test
    void checkJsonFormatOnSerialize() throws JsonProcessingException {


        List<MetadataAttributeDto<?>> metadataAttributes = new ArrayList<>();
        metadataAttributes.add(new MetadataAttributeDto<>("dummy", "test"));
        QuestionnaireMetadataDto questionnaireMetadataDto = new QuestionnaireMetadataDto(
                "household",
                metadataAttributes
        );

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(QuestionnaireMetadataDto.class, new QuestionnaireMetadataSerializer());
        mapper.registerModule(module);

        String metadata = mapper.writeValueAsString(questionnaireMetadataDto);

        assertEquals("""
                        {
                            "inseeContext":"household",
                            "variables": [
                                {"name":"dummy","value": "test"}
                            ]
                        }"""
                        .replaceAll("\\s+", "")
                , metadata.replaceAll("\\s+", ""));
    }
}
