package ioioi.it.mltraining.utils;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.text.MessageFormat;

public class LocaleUtils {
    private static final ResourceBundleMessageSource messageSource;

    static {
        messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
    }

    public static String getMessage(String messageCode) {
        return messageSource.getMessage(messageCode, null, LocaleContextHolder.getLocale());
    }

    public static String getMessage(String messageCode, Object... params) {
        return MessageFormat.format(getMessage(messageCode), params);
    }
}
