package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="questionnaire")
public class QuestionnaireEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="questionnaire_pogues_id")
    @NotNull
    private String questionnaireId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "campaign_id", referencedColumnName = "id")
    private CampaignEntity campaign;

    @Column
    @NotNull
    private String label;

    @OneToOne
    @JoinColumn(name = "context_id", referencedColumnName = "id")
    @NotNull
    private ContextEntity context;

    @ManyToMany
    @JoinTable(
        name = "questionnaire_mode", 
        joinColumns = @JoinColumn(name = "questionnaire_id"), 
        inverseJoinColumns = @JoinColumn(name = "mode_id"))
    @NotNull
    private List<ModeEntity> modes;

    @Lob
    @Column
    @NotNull
    private byte[] surveyUnitData;

    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    public QuestionnaireEntity(String questionnaireId, CampaignEntity campaign, String label, ContextEntity context, List<ModeEntity> modes, byte[] csvContent) {
        this.questionnaireId = questionnaireId;
        this.label = label;
        this.context = context;
        this.modes = modes;
        this.surveyUnitData = csvContent;
        this.campaign = campaign;
    }
}
