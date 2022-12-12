package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import fr.insee.publicenemy.api.application.domain.model.Context;
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
@Table(name="context")
public class ContextEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String value;

    public ContextEntity(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public static ContextEntity createFromModel(Context context) {
        return new ContextEntity(context.getId(), context.getValue());
    }

    public Context toModel() {
        return new Context(getId(), getValue());
    }
}
