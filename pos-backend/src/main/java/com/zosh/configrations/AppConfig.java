package com.zosh.configrations;



import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new CaseInsensitiveEnumConverterFactory());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

        @Override
        public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
            return source -> {
                if (source == null) {
                    return null;
                }

                String normalized = source.trim();
                if (normalized.isEmpty()) {
                    return null;
                }

                return (T) Enum.valueOf(targetType, normalized.toUpperCase(Locale.ROOT));
            };
        }
    }
}
