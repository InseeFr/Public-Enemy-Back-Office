package fr.insee.publicenemy.api.infrastructure.i18n;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import fr.insee.publicenemy.api.application.ports.I18nMessagePort;

@Component
public class I18nMessageServiceImpl implements I18nMessagePort {

    @Autowired
    private MessageSource messageSource;

    public String getMessage(String id) {
        return getMessage(id, null);
    }

    public String getMessage(String id, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(id, args, locale);
    }

    public String getMessage(MessageSourceResolvable msr) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msr, locale);
    }

    public String getMessage(String id, Object[] args, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(id, args, defaultMessage, locale);
    }
}