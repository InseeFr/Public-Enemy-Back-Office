package fr.insee.publicenemy.api.application.domain.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Ddi {    
    private String questionnaireId;
    private String label;
    private List<String> modes;
    private byte[] content;
}
