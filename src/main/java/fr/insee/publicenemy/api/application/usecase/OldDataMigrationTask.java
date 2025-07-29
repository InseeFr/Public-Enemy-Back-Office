package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.domain.utils.InterrogationData;
import fr.insee.publicenemy.api.application.ports.InterrogationCsvPort;
import fr.insee.publicenemy.api.application.ports.PersonalizationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

@Component
@ConditionalOnProperty(name = "application.dataMigration", havingValue = "true")
@Slf4j
public class OldDataMigrationTask implements ApplicationRunner {

    private final QueenUseCase queenUseCase;
    private final QuestionnaireUseCase questionnaireUseCase;
    private final InterrogationCsvPort interrogationCsvPort;
    private final PersonalizationPort personalizationPort;

    public OldDataMigrationTask(QuestionnaireUseCase questionnaireUseCase,
                                QueenUseCase queenUseCase,
                                InterrogationCsvPort interrogationCsvPort,
                                PersonalizationPort personalizationPort) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.queenUseCase = queenUseCase;
        this.interrogationCsvPort = interrogationCsvPort;
        this.personalizationPort = personalizationPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Questionnaire> questionnaires = questionnaireUseCase.getQuestionnaires();
        questionnaires.forEach(this::migrateExistingQuestionnaire);

    }

    private void migrateExistingQuestionnaire(Questionnaire questionnaire){
        log.info("Migrate questionnaire" + questionnaire);
        byte[] interrogationData =  questionnaireUseCase.getInterrogationData(questionnaire.getId());
        if(InterrogationData.FormatType.CSV.equals(InterrogationData.getDataFormat(interrogationData))){
            int nbInterroByMode = interrogationCsvPort.initInterrogations(interrogationData, "dummy").size();

            questionnaire.getQuestionnaireModes().forEach( qMode -> {
                IntStream.range(0, nbInterroByMode)
                        .mapToObj(index -> queenUseCase.getInterrogationsBySurveyUnit(
                                        String.format("%s-%s-%s", questionnaire.getId(), qMode.getMode().name(), index + 1)).stream()
                                .map(interroSu -> new PersonalizationMapping(interroSu.interrogationId(), questionnaire.getId(), qMode.getMode(), index))
                                .toList())
                        .flatMap(Collection::stream)
                        .forEach(this::savePersonalisationMapping);
            });
        }

    }

    private void savePersonalisationMapping(PersonalizationMapping personalizationMapping){
        personalizationPort.addPersonalizationMapping(personalizationMapping);
    }
}
