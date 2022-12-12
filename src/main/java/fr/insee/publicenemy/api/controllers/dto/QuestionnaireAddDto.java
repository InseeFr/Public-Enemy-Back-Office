package fr.insee.publicenemy.api.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireAddDto {
    private String questionnaireId;
    private Long contextId;
}
