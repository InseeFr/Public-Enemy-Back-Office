package fr.insee.publicenemy.api.infrastructure.questionnaire.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="campaign")
public class CampaignEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column
    private String label;

    public static CampaignEntity createWithLabel(String label) {
        CampaignEntity campaign = new CampaignEntity();
        campaign.setLabel(label);
        return campaign;
    }
}
