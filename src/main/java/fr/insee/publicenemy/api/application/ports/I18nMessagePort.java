package fr.insee.publicenemy.api.application.ports;

public interface I18nMessagePort {
    
    public String getMessage(String id);

    public String getMessage(String id, Object[] args);

    public String getMessage(String id, Object[] args, String defaultMessage);
}
