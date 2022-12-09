package fr.insee.publicenemy.api.application.ports;

import java.util.List;

import fr.insee.publicenemy.api.application.domain.model.Context;
import fr.insee.publicenemy.api.application.domain.model.Mode;
import fr.insee.publicenemy.api.application.domain.model.Questionnaire;

public interface QuestionnairePort {
    /**
     * Add questionnaire
     * @param questionnaire
     * @return saved questionnaire
     */
    public Questionnaire addQuestionnaire(Questionnaire questionnaire);


    /**
     * update questionnaire
     * @param questionnaire
     * @return saved questionnaire
     */
    public Questionnaire updateQuestionnaire(Questionnaire questionnaire);

    /**
     * 
     * @return all modes
     */
    public List<Mode> getModes();

    /**
     * 
     * @return all contexts
     */
    public List<Context> getContexts();

    /**
     * 
     * @param id
     * @return context by id
     */
    public Context getContext(Long id);

    /**
     * get mode by id
     * @param id
     * @return mode
     */
    public Mode getMode(Long id);
    
    /**
     * get mode by name
     * @param name
     * @return mode
     */
    public Mode getModeByName(String name);
}