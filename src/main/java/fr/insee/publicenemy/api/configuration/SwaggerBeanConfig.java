package fr.insee.publicenemy.api.configuration;

import java.util.ArrayList;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class SwaggerBeanConfig {
    
    /**
     * Used to workaround the issue where swagger does not handle correctly multipart request
     * https://github.com/swagger-api/swagger-ui/issues/6462
     * @param converter
     */  
    public SwaggerBeanConfig(MappingJackson2HttpMessageConverter converter) {
        var supportedMediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
        supportedMediaTypes.add(new MediaType("application", "octet-stream"));
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }
}
