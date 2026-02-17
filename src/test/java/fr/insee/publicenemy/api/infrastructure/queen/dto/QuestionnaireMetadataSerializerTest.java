package fr.insee.publicenemy.api.infrastructure.queen.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QuestionnaireMetadataSerializerTest {

    private static final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void checkJsonFormatOnSerialize() {

        List<MetadataAttributeDto<?>> metadataAttributes = new ArrayList<>();
        metadataAttributes.add(new MetadataAttributeDto<>("dummy", "test"));
        QuestionnaireMetadataDto questionnaireMetadataDto = new QuestionnaireMetadataDto(
                "household",
                metadataAttributes
        );

        String metadata = objectMapper.writeValueAsString(questionnaireMetadataDto);

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
