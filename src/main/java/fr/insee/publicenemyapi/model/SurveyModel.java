package fr.insee.publicenemyapi.model;

import org.json.simple.JSONObject;

public class SurveyModel {

    /**
     * The id of campaign
     */
    private String id;

    /**
     * The JSON Questionnaire of questionnaire model
     */
    private JSONObject value;

    public SurveyModel(String id, JSONObject value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject getValue() {
        return value;
    }

    public void setValue(JSONObject value) {
        this.value = value;
    }
}
