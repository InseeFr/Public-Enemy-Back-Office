package fr.insee.publicenemy.api.infrastructure.questionnaire;

import fr.insee.publicenemy.api.infrastructure.questionnaire.entity.QuestionnaireEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@NullMarked
public interface QuestionnaireEntityRepository extends JpaRepository<QuestionnaireEntity, Long> {
    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities order by q.id desc")
    List<QuestionnaireEntity> findAll();

    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities where q.id=?1")
    Optional<QuestionnaireEntity> findById(Long questionnaireId);

    @Query("select q from QuestionnaireEntity q join fetch q.modeEntities where q.poguesId=?1")
    Optional<QuestionnaireEntity> findByPoguesId(String poguesId);

    boolean existsByPoguesId(String poguesId);

    void deleteByPoguesId(String poguesId);
}
