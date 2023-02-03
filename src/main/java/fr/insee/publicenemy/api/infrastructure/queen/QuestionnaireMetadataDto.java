package fr.insee.publicenemy.api.infrastructure.queen;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.infrastructure.queen.dto.MetadataAttributeDto;
import lombok.NonNull;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public record QuestionnaireMetadataDto(String inseeContext, @JsonProperty("variables") List<MetadataAttributeDto<?>> metadataAttributes) {
    private static final Random random = new Random();

    /**
     * Create default metadata for a questionnaire, with predefined values
     * Some values are computed from the questionnaire
     * @param questionnaire
     * @param defaultMetadataAttributes
     * @return the default metadata for the questionnaire
     */
    public static QuestionnaireMetadataDto createDefaultQuestionnaireMetadata(Questionnaire questionnaire, @NonNull List<MetadataAttributeDto<String>> defaultMetadataAttributes) {
        LocalDate date = LocalDate.of(Year.now().getValue()+10, 1, 1);
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter dayMonthYearFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

        // Inject default values
        List<MetadataAttributeDto<?>> metadataAttributes = new ArrayList<>(defaultMetadataAttributes);
        // Inject dynamic values
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_LibelleEnquete", questionnaire.getLabel()));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_DateParutionJO", date.format(dayMonthYearFormatter)));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_ParutionJO",true));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_NumeroVisa", generateRandomIdentifier()));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_AnneeVisa", date.format(yearFormatter)));
        metadataAttributes.add(new MetadataAttributeDto<>("Enq_CaractereObligatoire",true));
        return new QuestionnaireMetadataDto(questionnaire.getContext().name().toLowerCase(),
                metadataAttributes);
    }

    /**
     * @return a generated random identifier
     */
    private static String generateRandomIdentifier() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}

