package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import fr.insee.publicenemy.api.application.domain.model.Mode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="mode")
public class ModeEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String value;

    public ModeEntity(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public static ModeEntity createFromModel(Mode mode) {
        return new ModeEntity(mode.getId(), mode.getValue());
    }

    public Mode toModel() {
        return new Mode(getId(), getValue());
    }
}
