package fr.insee.publicenemy.api.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.model.pogues.VariableType;
import fr.insee.publicenemy.api.application.domain.model.interrogation.Interrogation;
import fr.insee.publicenemy.api.application.domain.model.interrogation.InterrogationData;
import fr.insee.publicenemy.api.application.exceptions.ServiceException;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.infrastructure.csv.exceptions.InterrogationCsvNotFoundException;
import fr.insee.publicenemy.api.infrastructure.interro.InterrogationStateData;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class InterrogationCsvService implements InterrogationCsvPort {
    private final Integer iterationHeaderCount;

    private final I18nMessagePort messageService;

    private final static String INTERROGATION_HEADER = "ID_INTERROGATION";

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
            interrogations.add(initInterrogation(interrogationCsvLine, questionnaireModelId));
        }
        return interrogations;
    }

    @Override
    public void updateInterrogationData(Questionnaire questionnaire, List<Interrogation> interrogations) {
        try {
            byte[] interrogationDataUpdated = addInterrogationIdToData(questionnaire.getInterrogationData(), interrogations);
            questionnaire.setInterrogationData(interrogationDataUpdated);
        } catch (IOException e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "Data is invalid");
        }

    }

    @Override
    public Interrogation getCsvInterrogation(String interrogationId, byte[] interrogationData) {
        List<InterrogationCsvLine> interrogationsCsvLines = getInterrogationsCsvLines(interrogationData);

        Optional<InterrogationCsvLine> interrogationCsvLine = interrogationsCsvLines.stream()
                .filter(line -> line.getFields().containsMapping(INTERROGATION_HEADER, interrogationId))
                .findFirst();

        if(interrogationCsvLine.isEmpty()) throw new InterrogationCsvNotFoundException(messageService.getMessage("interrogation.not-found", interrogationId));
        return initInterrogation(interrogationCsvLine.get(), null);
    }

    /**
     * @param interrogationCsvLine    csv line containing a survey unit
     * @return a survey unit from a line in the csv file
     */
    private Interrogation initInterrogation(@NonNull InterrogationCsvLine interrogationCsvLine, String questionnaireModelId) {

        String interrogationId = "";

        List<Map.Entry<String, String>> csvFields = new ArrayList<>();
        if (interrogationCsvLine.getFields() != null) {
            csvFields = interrogationCsvLine.getFields().entries()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .toList();
        }

        if(interrogationCsvLine.getFields() != null){
            interrogationId = String.join("", interrogationCsvLine.getFields().get(INTERROGATION_HEADER));
        }

        if(interrogationId.isEmpty()){
            interrogationId = UUID.randomUUID().toString();
        }
        InterrogationData interrogationData = new InterrogationData(csvFields);
        return new Interrogation(interrogationId, questionnaireModelId, interrogationData, InterrogationStateData.createInitialStateData());
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

    public byte[] addInterrogationIdToData(byte[] csvInput, List<Interrogation> interrogations) throws IOException {
        // Read CSV line by line
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csvInput), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV is empty");
        }

        // if INTERROGATION_HEADER already exists, do nothing
        if(lines.getFirst().contains(INTERROGATION_HEADER)) return csvInput;

        // Append header column
        String header = lines.getFirst() + ",\"" +INTERROGATION_HEADER + "\"";

        // Validate matching size
        if (lines.size() - 1 != interrogations.size()) {
            throw new IllegalArgumentException("Number of data rows (" + (lines.size() - 1) +
                    ") does not match the number of ID values (" +
                    interrogations.size() + ")");
        }

        // Build new CSV content
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            writer.write(header);
            writer.write("\n");
            for (int i = 1; i < lines.size(); i++) {
                String newLine = lines.get(i) + ",\"" + interrogations.get(i - 1).id() + "\"";
                writer.write(newLine);
                writer.write("\n");
            }
        }

        return output.toByteArray();
    }
}
