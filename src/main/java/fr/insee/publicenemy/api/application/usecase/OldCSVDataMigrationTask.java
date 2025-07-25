package fr.insee.publicenemy.api.application.usecase;

import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import fr.insee.publicenemy.api.application.ports.I18nMessagePort;
import fr.insee.publicenemy.api.controllers.InterrogationMessagesComponent;
import fr.insee.publicenemy.api.controllers.exceptions.ApiExceptionComponent;
import fr.insee.publicenemy.api.infrastructure.queen.dto.InterrogationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "application.csvMigration", havingValue = "true")
@Slf4j
public class OldCSVDataMigrationTask implements ApplicationRunner {

    private final QueenUseCase queenUseCase;

    private final PoguesUseCase poguesUseCase;

    private final QuestionnaireUseCase questionnaireUseCase;

    private final InterrogationUseCase interrogationUseCase;

    public OldCSVDataMigrationTask(QuestionnaireUseCase questionnaireUseCase,
                                   QueenUseCase queenUseCase,
                                   InterrogationUseCase interrogationUseCase,
                                   PoguesUseCase poguesUseCase) {
        this.questionnaireUseCase = questionnaireUseCase;
        this.queenUseCase = queenUseCase;
        this.interrogationUseCase = interrogationUseCase;
        this.poguesUseCase = poguesUseCase;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Hello !");
        List<InterrogationDto> interrogationDtos = queenUseCase.getInterrogations("11-CAWI");
        List<Questionnaire> questionnaires = questionnaireUseCase.getQuestionnaires();
    }

    private void migrateCSVQuestionnaire(Questionnaire questionnaire){

    }
}
