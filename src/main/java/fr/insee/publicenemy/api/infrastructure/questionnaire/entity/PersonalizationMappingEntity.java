package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;


import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.PersonalizationMapping;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "personalization_mapping")
public class PersonalizationMappingEntity implements Serializable {
    @Id
    private UUID interrogationId;
    private Long questionnaireId;
    @Enumerated(EnumType.STRING)
    private Mode mode;
    private int dataIndex;

    public PersonalizationMapping toModel(){
        return new PersonalizationMapping(
                this.getInterrogationId().toString(),
                this.getQuestionnaireId(),
                this.getMode(),
                this.getDataIndex());
    }

    public static PersonalizationMappingEntity createEntity(@NonNull PersonalizationMapping mapping) {
        return new PersonalizationMappingEntity(
                UUID.fromString(mapping.interrogationId()),
                mapping.questionnaireId(),
                mapping.mode(),
                mapping.dataIndex());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PersonalizationMappingEntity that = (PersonalizationMappingEntity) o;
        return dataIndex == that.dataIndex && Objects.equals(interrogationId, that.interrogationId) && Objects.equals(questionnaireId, that.questionnaireId) && mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(interrogationId, questionnaireId, mode, dataIndex);
    }
}
