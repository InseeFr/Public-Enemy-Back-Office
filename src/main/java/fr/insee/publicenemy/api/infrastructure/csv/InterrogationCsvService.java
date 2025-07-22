package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationIdentifierHandler;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.exceptions.InterrogationCsvNotFoundException;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
@Slf4j
public class InterrogationCsvService implements InterrogationCsvPort {
    private final Integer iterationHeaderCount;

    private final I18nMessagePort messageService;

    public InterrogationCsvService(@Value("${application.csv.iteration-header-count}") Integer iterationHeaderCount, I18nMessagePort messagePort) {
        this.messageService = messagePort;
        this.iterationHeaderCount = iterationHeaderCount;
    }

    @Override
    public List<Interrogation> initInterrogations(byte[] interrogationData, String questionnaireModelId) {
        List<InterrogationCsvLine> interrogationsCsvLines = getInterrogationsCsvLines(interrogationData);

        List<Interrogation> interrogations = new ArrayList<>();
        for (int id = 1; id <= interrogationsCsvLines.size(); id++) {
            InterrogationCsvLine interrogationCsvLine = interrogationsCsvLines.get(id - 1);
            interrogations.add(initInterrogation(id, interrogationCsvLine, questionnaireModelId));
        }
        return interrogations;
    }

    @Override
    public Interrogation getCsvInterrogation(int interrogationId, byte[] interrogationData, String questionnaireModelId) {
        List<InterrogationCsvLine> interrogationsCsvLines = getInterrogationsCsvLines(interrogationData);

        if (interrogationId <= 0 || interrogationId > interrogationsCsvLines.size()) {
            throw new InterrogationCsvNotFoundException(messageService.getMessage("interrogation.not-found"));
        }

        InterrogationCsvLine interrogationCsvLine = interrogationsCsvLines.get(interrogationId - 1);
        return initInterrogation(interrogationId, interrogationCsvLine, questionnaireModelId);
    }

    /**
     * @param interrogationId         survey unit id
     * @param interrogationCsvLine    csv line containing a survey unit
     * @param questionnaireModelId questionnaire model id
     * @return a survey unit from a line in the csv file
     */
    private Interrogation initInterrogation(int interrogationId, @NonNull InterrogationCsvLine interrogationCsvLine, String questionnaireModelId) {
        String queenIdentifier = interrogationId + "";
        if (questionnaireModelId != null && !questionnaireModelId.isEmpty()) {
            InterrogationIdentifierHandler identifierHandler = new InterrogationIdentifierHandler(questionnaireModelId, interrogationId);
            queenIdentifier = identifierHandler.getQueenIdentifier();
        }

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        if (interrogationCsvLine.getFields() != null) {
            csvFields = interrogationCsvLine.getFields().entries()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
        }

        InterrogationData interrogationData = new InterrogationData(csvFields);
        return new Interrogation(queenIdentifier, questionnaireModelId, interrogationData, InterrogationStateData.createInitialStateData());
    }

    @Override
    public InterrogationCsvHeaderLine getInterrogationsCsvHeaders(List<VariableType> variablesType) {
        Set<String> csvHeaders = new LinkedHashSet<>();
        variablesType.forEach(
                variableType -> csvHeaders.addAll(getCsvHeaders(variableType)));
        return new InterrogationCsvHeaderLine(csvHeaders);
    }

    /**
     * Get CSV Headers for a specific variable type
     *
     * @param variableType variable type
     */
    private Set<String> getCsvHeaders(VariableType variableType) {
        Set<String> csvHeaders = new LinkedHashSet<>();
        if (!variableType.hasMultipleValues()) {
            csvHeaders.add(variableType.name());
            return csvHeaders;
        }

        // if variable type is an iteration, generate a specific number of headers
        for (int index = 1; index <= iterationHeaderCount; index++) {
            csvHeaders.add(variableType.name() + "_" + index);
        }
        return csvHeaders;
    }

    /**
     * get a list of interrogations from interrogation csv data
     *
     * @param interrogationData interrogations csv data
     * @return list of interrogations from interrogation csv data
     */
    private List<InterrogationCsvLine> getInterrogationsCsvLines(byte[] interrogationData) {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(interrogationData));

        return new CsvToBeanBuilder<InterrogationCsvLine>(reader)
                .withSkipLines(0)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withStrictQuotes(true)
                .withIgnoreQuotations(false)
                .withType(InterrogationCsvLine.class)
                .build().parse();
    }
}
