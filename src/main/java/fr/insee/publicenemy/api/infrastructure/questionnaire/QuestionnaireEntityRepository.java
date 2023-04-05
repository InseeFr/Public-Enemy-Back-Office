package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireEntityRepository extends JpaRepository<QuestionnaireEntity, Long> {
    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities order by q.id desc")
    @NonNull
    List<QuestionnaireEntity> findAll();

    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities where q.id=?1")
    @NonNull
    Optional<QuestionnaireEntity> findById(Long questionnaireId);

    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities where q.poguesId=?1")
    @NonNull
    Optional<QuestionnaireEntity> findByPoguesId(String poguesId);
} 
