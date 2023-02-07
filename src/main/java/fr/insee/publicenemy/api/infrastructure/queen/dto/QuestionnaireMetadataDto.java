package fr.insee.publicenemy.api.infrastructure.queen.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.utils.IdentifierGenerationUtils;
import lombok.NonNull;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
@JsonSerialize(using = QuestionnaireMetadataSerializer.class)
public record QuestionnaireMetadataDto(String inseeContext, List<MetadataAttributeDto<?>> metadataAttributes) {

    /**
     * Create default metadata for a questionnaire, with predefined values
     * Some values are computed from the questionnaire
     * @param questionnaire questionnaire
     * @param defaultMetadataAttributes default metadata attributes
     * @return the default metadata for the questionnaire
     */
    public static QuestionnaireMetadataDto createDefaultQuestionnaireMetadata(Questionnaire questionnaire, @NonNull List<MetadataAttributeDto<String>> defaultMetadataAttributes) {
        LocalDate date = LocalDate.of(Year.now().getValue()+10, 1, 1);
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter dayMonthYearFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Inject default values
        List<MetadataAttributeDto<?>> metadataAttributes = new ArrayList<>(defaultMetadataAttributes);
        // Inject dynamic values
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_LibelleEnquete", questionnaire.getLabel()));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_DateParutionJO", date.format(dayMonthYearFormatter)));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_ParutionJO",true));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_NumeroVisa", IdentifierGenerationUtils.generateRandomIdentifier()));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_AnneeVisa", date.format(yearFormatter)));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_CaractereObligatoire",true));
        return new QuestionnaireMetadataDto(questionnaire.getContext().name().toLowerCase(),
                metadataAttributes);
    }
}

