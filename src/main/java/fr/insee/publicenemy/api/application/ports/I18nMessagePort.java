package fr.insee.publicenemy.api.application.ports;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

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
    String getMessage(String id, String... args);

    String getMessage(MessageSourceResolvable msr);

    /**
     * 
     * @param id message key
     * @param args parameters for the message
     * @param defaultMessage failback message
     * @return message in default language
     */
    String getMessage(String id, String[] args, String defaultMessage);
}
