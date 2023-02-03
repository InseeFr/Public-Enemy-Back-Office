package fr.insee.publicenemy.api.configuration;

import fr.insee.publicenemy.api.infrastructure.queen.dto.MetadataAttributeDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "application.questionnaire")
public class MetadataProps {
    private List<MetadataAttributeDto<String>> metadata;

    public List<MetadataAttributeDto<String>> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetadataAttributeDto<String>> metadata) {
        this.metadata = metadata;
    }
}
