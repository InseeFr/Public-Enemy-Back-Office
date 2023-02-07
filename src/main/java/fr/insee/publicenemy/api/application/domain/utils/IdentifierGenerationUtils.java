package fr.insee.publicenemy.api.application.domain.utils;

import fr.insee.publicenemy.api.application.domain.model.Mode;

import java.util.Random;

public class IdentifierGenerationUtils {

    private IdentifierGenerationUtils() {
        throw new IllegalStateException("Utility class");
    }
    private static final Random random = new Random();

    /**
     *
     * @param questionnaireId Questionnaire on which an identifier will be created
     * @param mode mode
     * @return campaign/questionnaire-model identifier for queen
     */
    public static String generateQueenIdentifier(Long questionnaireId, Mode mode) {
        return String.format("%s-%s", questionnaireId, mode.name());
    }

    /**
     * @return a generated random identifier
     */
    public static String generateRandomIdentifier() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
