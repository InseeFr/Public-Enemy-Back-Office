package fr.insee.publicenemy.api.application.domain.model;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Questionnaire {

    private Long id;

    @NotNull
    private String questionnaireId;

    @NotNull
    private String label;

    @NotNull
    private Context context;

    @NotNull
    private List<Mode> modes;

    @NotNull
    private byte[] surveyUnitData;

    private Date updatedDate;

    public Questionnaire(String questionnaireId, String label, Context context, List<Mode> modes, byte[] csvContent) {
        this.questionnaireId = questionnaireId;
        this.label = label;
        this.context = context;
        this.modes = modes;
        this.surveyUnitData = csvContent;
    }

    public Questionnaire(Long id, Context context, byte[] surveyUnitData) {
        this.id = id;
        this.context = context;
        this.surveyUnitData = surveyUnitData;
    }
}
