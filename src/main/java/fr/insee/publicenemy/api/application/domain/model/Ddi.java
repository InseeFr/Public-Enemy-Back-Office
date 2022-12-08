package fr.insee.publicenemy.api.application.domain.model;

import java.util.List;

public class Ddi {
    private byte[] content;
    private String questionnaireId;
    private String label;
    private List<String> modes;

    public Ddi(String questionnaireId, String label, List<String> modes, byte[] content) {
        this.questionnaireId = questionnaireId;
        this.label = label;
        this.content = content;
        this.modes = modes;
    }

    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }
    public String getQuestionnaireId() {
        return questionnaireId;
    }
    public void setQuestionnaireId(String questionnaireId) {
        this.questionnaireId = questionnaireId;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public List<String> getModes() {
        return modes;
    }
    public void setModes(List<String> modes) {
        this.modes = modes;
    }
}
