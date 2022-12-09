package fr.insee.publicenemy.api.infrastructure.questionnaire;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.ModeEntity;

public interface ModeEntityRepository extends JpaRepository<ModeEntity, Long> {
    @Query("select m from ModeEntity m where m.name= :name")
    Optional<ModeEntity> findByName(@Param("name") String name);
} 
