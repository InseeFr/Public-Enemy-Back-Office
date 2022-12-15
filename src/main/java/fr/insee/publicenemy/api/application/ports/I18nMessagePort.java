package fr.insee.publicenemy.api.application.ports;

public interface I18nMessagePort {
    /**
     * 
     * @param id message key
     * @return message in default language
     */
    String getMessage(String id);
    
    /**
     * 
     * @param id message key
     * @param args parameters for the message
     * @return message in default language
     */
    String getMessage(String id, Object[] args);

    /**
     * 
     * @param id message key
     * @param args parameters for the message
     * @param defaultMessage failback message
     * @return message in default language
     */
    String getMessage(String id, Object[] args, String defaultMessage);
}
