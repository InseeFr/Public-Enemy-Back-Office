package fr.insee.publicenemy.api.infrastructure.pogues;

import fr.insee.publicenemy.api.application.domain.model.Mode;

import java.util.List;


public record PoguesDataSummary(String label, List<Mode> modes) {
}
