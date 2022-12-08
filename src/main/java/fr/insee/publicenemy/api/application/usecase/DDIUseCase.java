package fr.insee.publicenemy.api.application.usecase;

import org.springframework.stereotype.Service;

import fr.insee.publicenemy.api.application.domain.model.Ddi;
import fr.insee.publicenemy.api.application.ports.DdiServicePort;

@Service
public class DDIUseCase {
    
    private DdiServicePort ddiService;

    public DDIUseCase(DdiServicePort ddiService) {
        this.ddiService = ddiService;
    }

    public Ddi getDdi(String questionnaireId) {
        return ddiService.getDdi(questionnaireId);
    }
}
