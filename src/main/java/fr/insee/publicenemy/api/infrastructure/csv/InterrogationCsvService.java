package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableTypeEnum;
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

        return interrogationsCsvLines.stream()
                .map(line -> initInterrogation(line, null, questionnaireModelId))
                .toList();
    }

    @Override
    public Interrogation getCsvInterrogation(PersonalizationMapping persoMapping, byte[] interrogationData) {
        List<InterrogationCsvLine> interrogationsCsvLines = getInterrogationsCsvLines(interrogationData);

        if(interrogationsCsvLines.isEmpty() || persoMapping.dataIndex() >= interrogationsCsvLines.size() || persoMapping.dataIndex() < 0) throw new InterrogationCsvNotFoundException(messageService.getMessage("interrogation.not-found", persoMapping.interrogationId()));
        return initInterrogation(interrogationsCsvLines.get(persoMapping.dataIndex()), persoMapping.interrogationId(), persoMapping.getQuestionnaireModelId());
    }

    /**
     * @param interrogationCsvLine    csv line containing a survey unit
     * @return a survey unit from a line in the csv file
     */
    private Interrogation initInterrogation(@NonNull InterrogationCsvLine interrogationCsvLine, String interrogationId, String questionnaireModelId) {

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        if (interrogationCsvLine.getFields() != null) {
            csvFields = interrogationCsvLine.getFields().entries()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
        }

        String interroId = interrogationId != null ? interrogationId : UUID.randomUUID().toString();
        InterrogationData interrogationData = new InterrogationData(csvFields);
        return new Interrogation(interroId, questionnaireModelId, interrogationData, InterrogationStateData.createInitialStateData());
    }

    @Override
    public InterrogationCsvHeaderLine getInterrogationsCsvHeaders(List<VariableType> variablesType) {
        Set<String> csvHeaders = new LinkedHashSet<>();
        // Keep only external variables
        variablesType.stream()
                .filter(variable -> VariableTypeEnum.EXTERNAL.equals(variable.type()))
                .forEach(variableType -> csvHeaders.addAll(getCsvHeaders(variableType)));
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
